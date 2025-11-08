package ai.workley.gateway.chat.application.services;

import ai.workley.gateway.chat.application.ports.MessagePort;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.events.ReplyCompleted;
import ai.workley.gateway.chat.domain.events.ReplyStarted;
import ai.workley.gateway.chat.infrastructure.ai.AiModel;
import ai.workley.gateway.chat.infrastructure.intent.IntentClassification;
import ai.workley.gateway.chat.infrastructure.intent.IntentClassifier;
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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OllamaReplyStreamingService implements ReplyStreamingService {
    private static final Logger log = LoggerFactory.getLogger(OllamaReplyStreamingService.class);

    private final AiModel aiModel;
    private final MessagePort messagePort;
    private final IntentClassifier intentClassifier;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Sinks.Many<Message<String>> chatSink;

    public OllamaReplyStreamingService(
            AiModel aiModel,
            MessagePort messagePort,
            IntentClassifier intentClassifier,
            ApplicationEventPublisher applicationEventPublisher,
            Sinks.Many<Message<String>> chatSink
    ) {
        this.aiModel = aiModel;
        this.messagePort = messagePort;
        this.intentClassifier = intentClassifier;
        this.applicationEventPublisher = applicationEventPublisher;
        this.chatSink = chatSink;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyStarted e) {
        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        return messagePort.loadRecent(e.chatId(), 100)
                .collectList()
                .flatMapMany(history -> {
                    return intentClassifier.classify(e.message())
                            .timeout(Duration.ofSeconds(60))
                            .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal -> {
                                log.warn("Retrying classify intent (actor={}, chatId={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), retrySignal.totalRetries() + 1, retrySignal.failure().toString());
                            }))
                            .flatMap(classification -> {
                                log.info("Intent classified as {} with confidence {} (actor={}, chatId={})",
                                        classification.intent(), classification.confidence(), e.actor(), e.chatId());
                                return streamReply(e, classification, history);
                            })
                            .doOnError(err -> {
                                log.error("Intent classification failed (actor={}, chatId={})",
                                        e.actor(), e.chatId(), err);
                            });
                })
                .then();
    }

    private void emitChunkSafe(ReplyStarted e, String replyId, String chunk) {
        Message<String> dummy = Message.create(replyId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), chunk);
        Sinks.EmitResult emitResult = chatSink.tryEmitNext(dummy);
        if (emitResult.isFailure()) {
            log.warn("Dropped chunk (actor={}, chatId={}, reason={})", e.actor(), e.chatId(), emitResult);
        }
    }

    private Prompt buildPrompt(ReplyStarted e, IntentClassification classification, List<Message<String>> history) {
        List<org.springframework.ai.chat.messages.Message> list = new ArrayList<>();

        list.add(new SystemMessage(classification.getSystemPrompt()));

        for (Message<String> message : history) {
            switch (message.role()) {
                case ANONYMOUS,
                     CUSTOMER -> list.add(new UserMessage(message.content()));
                case ASSISTANT -> list.add(new AssistantMessage(message.content()));
                default -> log.warn("Ignoring role in history: {}", message.role());
            }
        }

        if (history.isEmpty() || !history.getLast().id().equals(e.message().id())) {
            list.add(new UserMessage(e.message().content()));
        }

        return new Prompt(list);
    }

    private Mono<Void> streamReply(ReplyStarted e, IntentClassification classification, List<Message<String>> history) {
        final String replyId = UUID.randomUUID().toString();

        Flux<String> chunks = aiModel.stream(buildPrompt(e, classification, history))
                .timeout(Duration.ofSeconds(30), Flux.empty())
                .publish()
                .autoConnect()
                .flatMapIterable(resp -> {
                    List<Generation> gens = resp != null
                            ? resp.getResults()
                            : null;
                    return gens != null ? gens : List.of();
                })
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .filter(chunk -> chunk != null && !chunk.isBlank())
                .doOnNext(chunk -> emitChunkSafe(e, replyId, chunk))
                .doOnError(error ->
                        log.error("Stream message failed (actor={}, chatId={})",
                                e.actor(), e.chatId(), error))
                .onErrorResume(error -> Flux.empty())
                .share();

        return chunks
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString)
                .defaultIfEmpty("")
                .flatMap(fullReply -> {
                    applicationEventPublisher.publishEvent(
                            new ReplyCompleted(e.actor(),
                                    e.chatId(), Message.create(replyId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), fullReply)));
                    return Mono.empty();
                })
                .then();
    }
}
