package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Command;
import ai.workley.core.chat.model.Payload;
import ai.workley.core.chat.model.ApplicationError;
import ai.workley.core.idempotency.IdempotencyGuard;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AppCommandBus implements CommandBus {
    private final Map<Class<? extends Command>, CommandHandler<?, ?>> handlers;
    private final IdempotencyGuard idempotencyGuard;

    public AppCommandBus(List<CommandHandler<?, ?>> source, IdempotencyGuard idempotencyGuard) {
        this.handlers = source.stream()
                .collect(Collectors.toMap(CommandHandler::supported,
                        Function.identity()));
        this.idempotencyGuard = idempotencyGuard;
    }

    public <T extends Command, R extends Payload> Mono<R> execute(String actor, T command) {
        return execute(actor, command, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Command, R extends Payload> Mono<R> execute(String actor, T command, String idempotencyKey) {
        CommandHandler<T, R> commandHandler = (CommandHandler<T, R>) handlers.get(command.getClass());

        if (commandHandler == null) {
            throw new ApplicationError(
                    "No handler found for command type " + command.getClass().getSimpleName()
            );
        }

        return idempotencyGuard.tryAcquire(idempotencyKey)
                .map(cached -> (R) cached)
                .switchIfEmpty(
                        commandHandler.handle(actor, command)
                                .flatMap(payload ->
                                        idempotencyGuard.markCompleted(idempotencyKey, payload)
                                                .thenReturn(payload))
                                .onErrorResume(error ->
                                        idempotencyGuard.markFailed(idempotencyKey)
                                                .then(Mono.error(error)))
                );
    }
}
