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
                        .maxBackoff(Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying AddMessage command (actor={}, chatId={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));

        AddMessage command =
                new AddMessage(e.chatId(),
                        Message.create(randomIdGenerator.generate(), e.chatId(), e.actor(), Role.ANONYMOUS, Instant.now(), e.prompt()));

        return addMessage(e.actor(), e.chatId(), command, retry);
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

        GenerateReply command = new GenerateReply(e.chatId(), e.message());

        return commandBus.execute(e.actor(), command)
                .timeout(Duration.ofSeconds(60))
                .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal ->
                        log.warn("Retrying GenerateReply command (actor={}, chatId={}, message={}) attempt #{} due to {}",
                                e.actor(), e.chatId(), e.message().content(),
                                retrySignal.totalRetries() + 1, retrySignal.failure().toString()))
                )
                .doOnSubscribe(subscription ->
                        log.info("Dispatching GenerateReply command: actor={}, chatId={}",
                                e.actor(), e.chatId()))
                .doOnSuccess(payload ->
                        log.info("GenerateReply dispatched successfully: actor={}, chatId={}",
                                e.actor(), e.chatId()))
                .doOnError(error ->
                        log.error("GenerateReply failed: actor={}, chatId={}, error={}",
                                e.actor(), e.chatId(), error.getMessage(), error))
                .onErrorResume(error -> Mono.empty())
                .then();
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyCompleted e) {
        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        AddMessage command =
                new AddMessage(e.chatId(),
                        Message.create(randomIdGenerator.generate(), e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), e.message().content()));

        return addMessage(e.actor(), e.chatId(), command, retryBackoffSpec);
    }


    private Mono<Void> addMessage(String actor, String chatId, AddMessage command, RetryBackoffSpec retryBackoffSpec) {
        return commandBus.execute(actor, command)
                .timeout(Duration.ofSeconds(60))
                .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal ->
                        log.warn("Retrying AddMessage (actor={}, chatId={}) attempt #{} due to {}",
                                actor, chatId, retrySignal.totalRetries() + 1, retrySignal.failure().toString()))
                )
                .doOnSubscribe(subscription ->
                        log.info("Dispatching AddMessage command: actor={}, chatId={}",
                                actor, chatId))
                .doOnSuccess(payload ->
                        log.info("AddMessage dispatched successfully: actor={}, chatId={}",
                                actor, chatId))
                .doOnError(error ->
                        log.error("AddMessage failed: actor={}, chatId={}, error={}",
                                actor, chatId, error.getMessage(), error))
                .onErrorResume(error -> Mono.empty())
                .then();
    }
}