package app.awaytogo.command.application.service;

import app.awaytogo.command.application.handler.CommandHandler;
import app.awaytogo.command.domain.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class CommandDispatcher {
    private static final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    private final Map<String, CommandHandler<?>> handlers;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public CommandDispatcher(Map<String, CommandHandler<?>> handlers, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.handlers = handlers;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @SuppressWarnings("unchecked")
    public Mono<String> dispatch(Command command) {
        String commandType = command.getClass().getSimpleName();

        CommandHandler<Command> commandHandler = (CommandHandler<Command>) handlers.get(commandType);

        if (commandHandler == null) {
            return Mono.error(new IllegalArgumentException(
                    "No handler found for command type: " + commandType
            ));
        }

        log.debug("Dispatching command {} to handler {}", commandType, commandHandler.getClass().getSimpleName());

        return commandHandler.handle(command)
                .doOnSuccess(result ->
                        log.debug("Command {} handled successfully",
                                command.getResumeId())
                )
                .doOnError(error ->
                        log.error("Error handling command {}: {}",
                                command.getResumeId(), error.getMessage()));
    }
}