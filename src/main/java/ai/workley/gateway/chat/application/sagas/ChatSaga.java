package ai.workley.gateway.chat.application.sagas;

import ai.workley.gateway.chat.application.ports.inbound.CommandBus;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.command.AddMessage;
import ai.workley.gateway.chat.domain.command.GenerateReply;
import ai.workley.gateway.chat.domain.content.TextContent;
import ai.workley.gateway.chat.domain.events.*;
import ai.workley.gateway.chat.infrastructure.id.IdGenerator;
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

    private static final RetryBackoffSpec RETRY_BACKOFF_SPEC =
            Retry.backoff(3, Duration.ofMillis(200))
                    .maxBackoff(Duration.ofSeconds(3))
                    .jitter(0.5)
//                    .filter(ex -> isRetriable(ex))
                    .onRetryExhaustedThrow((retrySpec, retrySignal) -> retrySignal.failure())
                    .doBeforeRetry(retrySignal -> {
                        log.warn("Retry {} due to {}",
                                retrySignal.totalRetries() + 1,
                                retrySignal.failure().toString());
                    });

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    public ChatSaga(CommandBus commandBus, IdGenerator idGenerator) {
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ChatCreated e) {
        AddMessage command =
                new AddMessage(e.chatId(),
                        Message.create(idGenerator.generate(), e.chatId(), e.actor(), Role.ANONYMOUS, Instant.now(), new TextContent(e.prompt())));

        return addMessage(e.actor(), e.chatId(), command);
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(MessageAdded e) {
        if (e.message().role() == Role.ASSISTANT) {
            return Mono.empty();
        }
        String idempotencyKey = idGenerator.generate();
        GenerateReply command = new GenerateReply(e.chatId(), e.message());
        return commandBus.execute(e.actor(), command, idempotencyKey)
                .timeout(Duration.ofSeconds(60))
                .retryWhen(RETRY_BACKOFF_SPEC)
                .doOnError(error ->
                        log.error("[{}] failed (actor={}, chatId={}, error={})",
                                command.getClass().getSimpleName(), e.actor(), e.chatId(), error.getMessage(), error))
                .onErrorResume(error -> Mono.empty())
                .then();
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyCompleted e) {
        AddMessage command =
                new AddMessage(e.chatId(),
                        e.message());

        return addMessage(e.actor(), e.chatId(), command);
    }


    private Mono<Void> addMessage(String actor, String chatId, AddMessage command) {
        String idempotencyKey = idGenerator.generate();
        return commandBus.execute(actor, command, idempotencyKey)
                .timeout(Duration.ofSeconds(60))
                .retryWhen(RETRY_BACKOFF_SPEC)
                .doOnError(error ->
                        log.error("[{}] failed (actor={}, chatId={}, error={})",
                                command.getClass().getSimpleName(), actor, chatId, error.getMessage(), error))
                .onErrorResume(error -> Mono.empty())
                .then();
    }
}
