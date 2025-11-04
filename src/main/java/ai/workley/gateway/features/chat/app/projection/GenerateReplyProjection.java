package ai.workley.gateway.features.chat.app.projection;

import ai.workley.gateway.features.chat.app.port.MessagePort;
import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.domain.event.ReplyFailed;
import ai.workley.gateway.features.shared.infra.error.InfrastructureErrors;
import ai.workley.gateway.features.chat.domain.Role;
import ai.workley.gateway.features.chat.domain.event.ReplyCompleted;
import ai.workley.gateway.features.chat.domain.event.ReplyGenerated;
import ai.workley.gateway.features.shared.infra.ai.AiModel;
import ai.workley.gateway.features.chat.infra.generators.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class GenerateReplyProjection {
    private static final Logger log = LoggerFactory.getLogger(GenerateReplyProjection.class);

    private final AiModel aiModel;
    private final IdGenerator messageIdGenerator;
    private final MessagePort messagePort;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Sinks.Many<Message<String>> chatSink;

    public GenerateReplyProjection(
            AiModel aiModel,
            MessagePort messagePort,
            IdGenerator messageIdGenerator,
            ApplicationEventPublisher applicationEventPublisher,
            Sinks.Many<Message<String>> chatSink

    ) {
        this.chatSink = chatSink;
        this.aiModel = aiModel;
        this.messagePort = messagePort;
        this.messageIdGenerator = messageIdGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    //    @Async
    @EventListener
    @Order(0)
    public Mono<Void> handle(ReplyGenerated e) {
        return messagePort.findRecentConversation(e.chatId(), 100)
                .collectList()
                .flatMapMany(history -> streamReply(e, history))
                .then();
    }

    private void emitChunkSafe(ReplyGenerated e, String id, String chunk) {
        Message<String> message = Message.create(id, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), chunk);
        Sinks.EmitResult emitResult = chatSink.tryEmitNext(message);
        if (emitResult.isFailure()) {
            log.warn("Dropped chunk (actor={}, chatId={}, reason={})", e.actor(), e.chatId(), emitResult);
        }
    }

    private Mono<Message<String>> saveMessage(ReplyGenerated e, String id, String content) {
        Message<String> message =
                Message.create(id, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), content);
        return messagePort.save(message)
                .map(saved ->
                        Message.create(
                                saved.id(), saved.chatId(), e.actor(), saved.role(), saved.createdAt(), saved.content()))
                .doOnSuccess(saved -> log.info("Reply saved: id={}, chatId={}", saved.id(), saved.chatId()))
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.warn("Reply already exists (actor={}, chatId={}, messageId={}, prompt={})",
                            e.actor(), e.chatId(), id, e.prompt(), error);
                    return Mono.empty();
                });
    }

    private Flux<Message<String>> streamReply(ReplyGenerated e, List<Message<String>> history) {
        final String messageId = messageIdGenerator.generate();

        Flux<String> chunks = aiModel.stream(buildPrompt(e, history))
                .timeout(Duration.ofSeconds(30), Flux.empty())
                .flatMapIterable(resp -> {
                    List<Generation> gens = resp != null
                            ? resp.getResults()
                            : null;
                    return gens != null ? gens : List.of();
                })
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .filter(chunk -> chunk != null && !chunk.isBlank())
                .doOnNext(chunk -> emitChunkSafe(e, messageId, chunk))
                .doOnError(error -> log.error("Stream reply failed (actor={}, chatId={})", e.actor(), e.chatId(), error))
                .onErrorResume(error -> Flux.empty())
                .share();

        Mono<String> fullReply = chunks
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString)
                .defaultIfEmpty("");

        return fullReply.flatMapMany(reply -> {
                    if (reply.isBlank()) {
                        applicationEventPublisher.publishEvent(new ReplyFailed(e.actor(), e.chatId(), "Reply is blank or not valid"));
                        return Flux.empty();
                    }
                    return saveMessage(e, messageId, reply)
                            .doOnNext(saved -> applicationEventPublisher.publishEvent(
                                    new ReplyCompleted(e.actor(), e.chatId(), saved)))
                            .onErrorResume(InfrastructureErrors::isDuplicateKey, cause -> {
                                log.warn("Duplicate reply id={} (chatId={}). Treating as success.", messageId, e.chatId(), cause);

                                applicationEventPublisher.publishEvent(
                                        new ReplyCompleted(e.actor(), e.chatId(), Message.create(messageId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), reply)));

                                return Mono.empty();
                            })
                            .flux();
                })
                .doFinally(signal -> {
                    if (signal == SignalType.ON_ERROR || signal == SignalType.CANCEL) {
                        applicationEventPublisher.publishEvent(new ReplyFailed(e.actor(), e.chatId(), signal.name()));
                    }
                });
    }

    private Prompt buildPrompt(ReplyGenerated e, List<Message<String>> history) {
        List<org.springframework.ai.chat.messages.Message> list = new ArrayList<>();
        list.add(new SystemMessage(e.classification().getSystemPrompt()));
        if (history.isEmpty()) {
            list.add(new UserMessage(e.prompt().content()));
        } else {
            for (Message<String> message : history) {
                switch (message.role()) {
                    case ANONYMOUS,
                         CUSTOMER -> list.add(new UserMessage(message.content()));
                    case ASSISTANT -> list.add(new AssistantMessage(message.content()));
                    default -> log.warn("Ignoring role in history: {}", message.role());
                }
            }
        }
        return new Prompt(list);
    }
}