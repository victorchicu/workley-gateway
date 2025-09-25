package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandDispatcher;
import ai.jobbortunity.gateway.chat.application.command.impl.AddMessageCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

@Component
public class CreateChatProcessManager {
    private static final Logger log = LoggerFactory.getLogger(CreateChatProcessManager.class);

    private final CommandDispatcher commandDispatcher;

    CreateChatProcessManager(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(CreateChatEvent e) {
        RetryBackoffSpec addMessageRetry =
                Retry.backoff(3, Duration.ofMillis(200))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying AddMessage (actor={}, chatId={}, messageId={}) attempt #{} due to {}",
                                        e.actor().getName(), e.chatId(), e.message().id(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));
        return commandDispatcher
                .dispatch(e.actor(), new AddMessageCommand(e.chatId(), e.message()))
                .timeout(Duration.ofSeconds(5))
                .retryWhen(addMessageRetry)
                .doOnSuccess(result ->
                        log.info("AddMessage succeeded (actor={}, chatId={}, message={})",
                                e.actor().getName(), e.chatId(), e.message()))
                .onErrorResume(error -> {
                    log.error("Giving up AddMessageCommand for (actor={}, chatId={})",
                            e.actor().getName(), e.chatId(), error);
                    return Mono.empty();
                })
                .then();
    }
}
