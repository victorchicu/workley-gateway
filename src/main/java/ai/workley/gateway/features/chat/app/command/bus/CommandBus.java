package ai.workley.gateway.features.chat.app.command.bus;

import ai.workley.gateway.features.shared.app.command.handler.CommandHandler;
import ai.workley.gateway.features.shared.app.command.results.Output;
import ai.workley.gateway.features.shared.domain.command.Command;
import ai.workley.gateway.features.chat.app.error.ApplicationError;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommandBus {
    private final Map<Class<? extends Command>, CommandHandler<?, ?>> handlers;

    public CommandBus(List<CommandHandler<?, ?>> source) {
        this.handlers = source.stream()
                .collect(Collectors.toMap(CommandHandler::supported,
                        Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Command, R extends Output> Mono<R> execute(String actor, T command) {
        CommandHandler<T, R> commandHandler = (CommandHandler<T, R>) handlers.get(command.getClass());

        if (commandHandler == null) {
            throw new ApplicationError(
                    "No handler found for command type " + command.getClass().getSimpleName()
            );
        }

        return commandHandler.handle(actor, command);
    }
}
