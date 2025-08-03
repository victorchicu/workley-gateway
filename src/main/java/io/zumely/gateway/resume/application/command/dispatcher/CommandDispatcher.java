package io.zumely.gateway.resume.application.command.dispatcher;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.command.handler.CommandHandler;
import io.zumely.gateway.resume.application.command.result.Result;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommandDispatcher {
    private final Map<Class<? extends Command>, CommandHandler<?, ?>> handlers;

    public CommandDispatcher(List<CommandHandler<?, ?>> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(CommandHandler::supported,
                        Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Command, R extends Result> R dispatch(T command) {
        CommandHandler<T, R> handler = (CommandHandler<T, R>) handlers.get(command.getClass());

        if (handler == null) {
            throw new ApplicationException(
                    "No handler found for command type: " + command.getClass().getSimpleName()
            );
        }

        return handler.handle(command);
    }
}
