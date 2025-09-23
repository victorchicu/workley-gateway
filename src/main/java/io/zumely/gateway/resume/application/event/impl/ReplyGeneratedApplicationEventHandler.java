package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.command.Role;
import io.zumely.gateway.resume.application.service.IdGenerator;
import io.zumely.gateway.resume.infrastructure.MessageHistoryRepository;
import io.zumely.gateway.resume.infrastructure.data.MessageObject;
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

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ReplyGeneratedApplicationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(ReplyGeneratedApplicationEventHandler.class);

    private final IdGenerator messageIdGenerator;
    private final OpenAiChatModel openAiChatModel;
    private final MessageHistoryRepository messageHistoryRepository;
    private final Sinks.Many<Message<String>> chatSink;

    public ReplyGeneratedApplicationEventHandler(
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
    public Mono<Void> handle(ReplyGeneratedApplicationEvent source) {
        Prompt prompt = Prompt.builder()
                .content(source.prompt())
                .build();

        return Mono.fromCallable(() -> new StreamContext(messageIdGenerator.generate(), source))
                .flatMap(context ->
                        openAiChatModel.stream(prompt)
                                .map(this::extractText)
                                .filter(Objects::nonNull)
                                .filter(text -> !text.isEmpty())
                                .doOnNext(chunk -> emitChunk(context, chunk))
                                .reduce("", String::concat)
                                .filter(content -> !content.isEmpty())
                                .flatMap(content -> saveCompleteMessage(context, content))
                                .then()
                );

    }

    private void emitChunk(StreamContext context, String chunk) {
        Message<String> message = Message.create(
                context.messageId,
                context.source.chatId(),
                context.source.actor().getName(),
                Role.ASSISTANT,
                Instant.now(),
                chunk
        );
        chatSink.tryEmitNext(message);
    }

    private Mono<Void> saveCompleteMessage(StreamContext context, String content) {
        Message<String> message = Message.create(
                context.messageId,
                context.source.chatId(),
                context.source.actor().getName(),
                Role.ASSISTANT,
                Instant.now(),
                content
        );
        return messageHistoryRepository.save(toMessageObject(message))
                .doOnSuccess((MessageObject<String> saved) -> {
                    log.info("Successfully saved {} event: {}",
                            context.source().getClass().getSimpleName(), context.source());
                })
                .then();
    }

    private String extractText(ChatResponse response) {
        if (response == null)
            return "";

        List<Generation> generations = response.getResults();
        if (generations.isEmpty()) {
            return "";
        }

        return generations.stream()
                .filter(Objects::nonNull)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }

    private MessageObject<String> toMessageObject(Message<String> source) {
        return MessageObject.create(source.id(), source.writtenBy(), source.chatId(), source.authorId(), source.createdAt(), source.content());
    }

    private record StreamContext(String messageId, ReplyGeneratedApplicationEvent source) {

    }
}
