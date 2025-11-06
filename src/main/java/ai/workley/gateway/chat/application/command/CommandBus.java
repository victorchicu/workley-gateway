package ai.workley.gateway.chat.application.command;

import ai.workley.gateway.chat.domain.command.Command;
import ai.workley.gateway.chat.domain.payloads.Payload;
import ai.workley.gateway.chat.application.exceptions.ApplicationError;
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
    public <T extends Command, R extends Payload> Mono<R> execute(String actor, T command) {
        CommandHandler<T, R> commandHandler = (CommandHandler<T, R>) handlers.get(command.getClass());

        if (commandHandler == null) {
            throw new ApplicationError(
                    "No handler found for command type " + command.getClass().getSimpleName()
            );
        }

        return commandHandler.handle(actor, command);
    }
}
