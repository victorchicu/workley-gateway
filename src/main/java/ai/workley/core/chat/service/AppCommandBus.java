package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Command;
import ai.workley.core.chat.model.Payload;
import ai.workley.core.chat.model.ApplicationError;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AppCommandBus implements CommandBus {
    private final Map<Class<? extends Command>, CommandHandler<?, ?>> handlers;

    public AppCommandBus(List<CommandHandler<?, ?>> source) {
        this.handlers = source.stream()
                .collect(Collectors.toMap(CommandHandler::supported,
                        Function.identity()));
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

        return commandHandler.handle(actor, command, idempotencyKey);
    }
}
