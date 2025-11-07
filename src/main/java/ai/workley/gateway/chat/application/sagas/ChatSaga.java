package ai.workley.gateway.chat.application.sagas;

import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.command.AddMessage;
import ai.workley.gateway.chat.domain.command.GenerateReply;
import ai.workley.gateway.chat.domain.events.*;
import ai.workley.gateway.chat.application.command.CommandBus;
import ai.workley.gateway.chat.infrastructure.generators.IdGenerator;
import ai.workley.gateway.chat.domain.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.time.Instant;

@Component
public class ChatSaga {
    private static final Logger log = LoggerFactory.getLogger(ChatSaga.class);

    private final CommandBus commandBus;
    private final IdGenerator randomIdGenerator;

    public ChatSaga(CommandBus commandBus, IdGenerator randomIdGenerator) {
        this.commandBus = commandBus;
        this.randomIdGenerator = randomIdGenerator;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ChatCreated e) {
        RetryBackoffSpec retry =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.5)
                        //.filter(this::isRetryable)
                        .maxBackoff(Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying adding message (actor={}, chatId={}, message={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), e.prompt(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));

        Message<String> message =
                Message.create(randomIdGenerator.generate(), e.chatId(), e.actor(), Role.ANONYMOUS, Instant.now(), e.prompt());

        return commandBus.execute(e.actor(), new AddMessage(e.chatId(), message))
                .timeout(Duration.ofSeconds(5))
                .retryWhen(retry)
                .doOnSuccess(result ->
                        log.info("Add message command executed (actor={}, chatId={}, message={})",
                                e.actor(), e.chatId(), e.prompt()))
                .onErrorResume(error -> {
                    log.error("Failed to add message even after all retry attempts (actor={}, chatId={}, message={})",
                            e.actor(), e.chatId(), e.prompt(), error);
                    return Mono.empty();
                })
                .then();
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(MessageAdded e) {
        if (e.message().role() == Role.ASSISTANT) {
            return Mono.empty();
        }

        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        return commandBus.execute(e.actor(), new GenerateReply(e.chatId(), e.message()))
                .timeout(Duration.ofSeconds(60))
                .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal ->
                        log.warn("Retrying generating message (actor={}, chatId={}, message={}) attempt #{} due to {}",
                                e.actor(), e.chatId(), e.message().content(),
                                retrySignal.totalRetries() + 1, retrySignal.failure().toString()))
                )
                .doOnSuccess(v ->
                        log.info("Generate reply command executed (actor={}, chatId={}, message={})",
                                e.actor(), e.chatId(), e.message().content()))
                .onErrorResume(error -> {
                    log.error("Generate reply failed even after all retry attempts (actor={}, chatId={}, message={})",
                            e.actor(), e.chatId(), e.message().content(), error);
                    return Mono.empty();
                })
                .then();
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyCompleted e) {
        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        return commandBus.execute(e.actor(), new AddMessage(e.chatId(), e.reply()))
                .timeout(Duration.ofSeconds(60))
                .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal ->
                        log.warn("Retrying saving message (actor={}, chatId={}, message={}) attempt #{} due to {}",
                                e.actor(), e.chatId(), e.reply().content(),
                                retrySignal.totalRetries() + 1, retrySignal.failure().toString()))
                )
                .doOnSuccess(v ->
                        log.info("Add message command executed (actor={}, chatId={}, message={})",
                                e.actor(), e.chatId(), e.reply().content()))
                .onErrorResume(error -> {
                    log.error("Add message failed even after all retry attempts (actor={}, chatId={}, message={})",
                            e.actor(), e.chatId(), e.reply().content(), error);
                    return Mono.empty();
                })
                .then();
    }
}