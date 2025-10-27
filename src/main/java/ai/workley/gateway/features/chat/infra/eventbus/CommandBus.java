package ai.workley.gateway.features.chat.infra.eventbus;

import ai.workley.gateway.features.shared.app.command.CommandHandler;
import ai.workley.gateway.features.shared.domain.command.Command;
import ai.workley.gateway.features.chat.domain.error.ApplicationError;
import ai.workley.gateway.features.shared.app.command.results.Result;
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
    public <T extends Command, R extends Result> Mono<R> execute(String actor, T command) {
        CommandHandler<T, R> commandHandler = (CommandHandler<T, R>) handlers.get(command.getClass());

        if (commandHandler == null) {
            throw new ApplicationError(
                    "No handler found for command type " + command.getClass().getSimpleName()
            );
        }

        return commandHandler.handle(actor, command);
    }
}
