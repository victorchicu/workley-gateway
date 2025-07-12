package ai.zumely.gateway.resume.application;

import ai.zumely.gateway.resume.domain.command.Command;
import ai.zumely.gateway.resume.domain.command.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Map;

@Component
public class CommandDispatcher {
    private static final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    private final Map<String, CommandHandler<? extends Command, ? extends Response>> commandHandlers;

    public CommandDispatcher(Map<String, CommandHandler<? extends Command, ? extends Response>> commandHandlers) {
        this.commandHandlers = commandHandlers;
    }

    @SuppressWarnings("unchecked")
    public <T extends Command, R extends Response> Mono<R> dispatch(Principal principal, T command) {
        String commandType = command.getClass().getSimpleName();

        CommandHandler<T, R> commandHandler = (CommandHandler<T, R>) commandHandlers.get(commandType);

        if (commandHandler == null) {
            return Mono.error(new IllegalArgumentException(
                    "No handler found for command type: " + commandType
            ));
        }

        log.debug("Dispatching command {} to handler {}",
                commandType, commandHandler.getClass().getSimpleName());

        return commandHandler.handle(principal, command)
                .doOnSuccess(response ->
                        log.debug("Command {} handled successfully",
                                command.getResumeId())
                )
                .doOnError(error ->
                        log.error("Error handling command {}: {}",
                                command.getResumeId(), error.getMessage()));
    }
}