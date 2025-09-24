package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.command.Role;
import ai.jobbortunity.gateway.chat.application.exception.ApplicationException;
import ai.jobbortunity.gateway.chat.application.service.IdGenerator;
import ai.jobbortunity.gateway.chat.infrastructure.MessageHistoryRepository;
import ai.jobbortunity.gateway.chat.infrastructure.data.MessageObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class GenerateReplyProjectionListener {
    private static final Logger log = LoggerFactory.getLogger(GenerateReplyProjectionListener.class);

    private final IdGenerator messageIdGenerator;
    private final OpenAiChatModel openAiChatModel;
    private final MessageHistoryRepository messageHistoryRepository;
    private final Sinks.Many<Message<String>> chatSink;

    public GenerateReplyProjectionListener(
            IdGenerator messageIdGenerator,
            OpenAiChatModel openAiChatModel,
            MessageHistoryRepository messageHistoryRepository,
            Sinks.Many<Message<String>> chatSink

    ) {
        this.chatSink = chatSink;
        this.openAiChatModel = openAiChatModel;
        this.messageIdGenerator = messageIdGenerator;
        this.messageHistoryRepository = messageHistoryRepository;
    }

    @EventListener
    public Mono<Void> handle(GenerateReplyEvent source) {
        Prompt prompt = Prompt.builder().content(source.prompt().content()).build();

        return Mono.defer(() -> Mono.just(new StreamContext(messageIdGenerator.generate(), source)))
                .flatMap(ctx ->
                        openAiChatModel.stream(prompt)
                                .timeout(Duration.ofSeconds(60))
                                .map(this::extractText)
                                .filter(Objects::nonNull)
                                .filter(s -> !s.isEmpty())
                                .doOnNext(chunk -> emitChunk(ctx, chunk))
                                .reduce(new StringBuilder(), StringBuilder::append)
                                .map(StringBuilder::toString)
                                .filter(content -> !content.isEmpty())
                                .flatMap(content -> saveFinalMessage(ctx, content))
                )
                .doOnError(err -> log.error("Reply generation failed: chatId={}", source.chatId(), err))
                .onErrorResume(err -> Mono.empty())
                .then();
    }

    private void emitChunk(StreamContext ctx, String chunk) {
        Message<String> message =
                Message.create(ctx.messageId, ctx.source.chatId(), ctx.source.actor().getName(), Role.ASSISTANT, Instant.now(), chunk);

        Sinks.EmitResult emitResult = chatSink.tryEmitNext(message);

        if (emitResult.isFailure()) {
            log.debug("Stream emit failed ({}): chatId={}, messageId={}",
                    emitResult, ctx.source.chatId(), ctx.messageId);
        }
    }

    private String extractText(ChatResponse response) {
        if (response == null) return "";
        List<Generation> generations = response.getResults();
        if (generations.isEmpty()) return "";
        return generations.stream()
                .filter(Objects::nonNull)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }

    private Mono<Message<String>> saveFinalMessage(StreamContext ctx, String content) {
        MessageObject<String> mo = MessageObject.create(
                ctx.messageId, Role.ASSISTANT, ctx.source.chatId(), ctx.source.actor().getName(), Instant.now(), content);

        return messageHistoryRepository.save(mo)
                .map(saved -> Message.create(
                        saved.getId(), saved.getChatId(), ctx.source.actor().getName(),
                        saved.getRole(), saved.getCreatedAt(), saved.getContent()))
                .onErrorMap(err -> new ApplicationException("Oops! Could not save your message.", err));
    }

    private record StreamContext(String messageId, GenerateReplyEvent source) {}
}
