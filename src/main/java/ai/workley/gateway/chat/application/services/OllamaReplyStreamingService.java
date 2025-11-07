package ai.workley.gateway.chat.application.services;

import ai.workley.gateway.chat.application.ports.MessagePort;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.events.ReplyCompleted;
import ai.workley.gateway.chat.domain.events.ReplyStarted;
import ai.workley.gateway.chat.infrastructure.ai.AiModel;
import ai.workley.gateway.chat.infrastructure.generators.IdGenerator;
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

@Service
public class OllamaReplyStreamingService implements ReplyStreamingService {
    private static final Logger log = LoggerFactory.getLogger(OllamaReplyStreamingService.class);

    private final AiModel aiModel;
    private final MessagePort messagePort;
    private final IdGenerator randomIdGenerator;
    private final IntentClassifier intentClassifier;
    private final ApplicationEventPublisher publisher;
    private final Sinks.Many<Message<String>> chatSink;

    public OllamaReplyStreamingService(
            AiModel aiModel,
            MessagePort messagePort,
            IdGenerator randomIdGenerator,
            IntentClassifier intentClassifier,
            ApplicationEventPublisher publisher,
            Sinks.Many<Message<String>> chatSink
    ) {
        this.aiModel = aiModel;
        this.messagePort = messagePort;
        this.randomIdGenerator = randomIdGenerator;
        this.intentClassifier = intentClassifier;
        this.publisher = publisher;
        this.chatSink = chatSink;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyStarted e) {
        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        return messagePort.loadRecentConversation(e.chatId(), 100)
                .collectList()
                .flatMapMany(history -> {
                    return intentClassifier.classify(e.message())
                            .timeout(Duration.ofSeconds(60))
                            .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal -> {
                                log.warn("Retrying classify intent (actor={}, chatId={}, message={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), e.message().content(),
                                        retrySignal.totalRetries() + 1, retrySignal.failure().toString());
                            }))
                            .doOnError(err -> {
                                log.error("Intent classification failed (actor={}, chatId={}, message={})",
                                        e.actor(), e.chatId(), e.message().content(), err);
                            })
                            .flatMap(classification -> {
                                log.info("Intent classified as {} with confidence {} (actor={}, chatId={}, message={})",
                                        classification.intent(), classification.confidence(),
                                        e.actor(), e.chatId(), e.message().content());
                                return streamReply(e, classification, history);
                            });
                })
                .then();
    }

    private void emitChunkSafe(ReplyStarted e, String id, String chunk) {
        Message<String> message = Message.create(id, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), chunk);
        Sinks.EmitResult emitResult = chatSink.tryEmitNext(message);
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
        final String messageId = randomIdGenerator.generate();

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
                .doOnNext(chunk -> emitChunkSafe(e, messageId, chunk))
                .doOnError(error ->
                        log.error("Stream message failed (actor={}, chatId={})",
                                e.actor(), e.chatId(), error))
                .onErrorResume(error -> Flux.empty())
                .share();

        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        return chunks
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString)
                .defaultIfEmpty("")
                .flatMap(reply -> emitReplyCompleted(e, reply, messageId))
                .then();
    }

    private Mono<Void> emitReplyCompleted(ReplyStarted e, String reply, String messageId) {
        Message<String> message = Message.create(messageId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), reply);
        publisher.publishEvent(new ReplyCompleted(e.actor(), e.chatId(), message));
        return Mono.empty();
    }
}

