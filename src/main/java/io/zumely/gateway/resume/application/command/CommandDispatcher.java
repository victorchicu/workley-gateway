package io.zumely.gateway.resume.application.command;

import io.zumely.gateway.resume.application.command.handler.CommandHandler;
import io.zumely.gateway.resume.application.command.data.CommandResult;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommandDispatcher {
    private final Map<Class<? extends Command>, CommandHandler<?, ?>> handlers;

    public CommandDispatcher(List<CommandHandler<?, ?>> source) {
        this.handlers = source.stream()
                .collect(Collectors.toMap(CommandHandler::supported,
                        Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Command, R extends CommandResult> Mono<R> dispatch(Principal actor, T command) {
        CommandHandler<T, R> commandHandler = (CommandHandler<T, R>) handlers.get(command.getClass());

        if (commandHandler == null) {
            throw new ApplicationException(
                    "No handler found for command type " + command.getClass().getSimpleName()
            );
        }

        return commandHandler.handle(actor, command);
    }
}
