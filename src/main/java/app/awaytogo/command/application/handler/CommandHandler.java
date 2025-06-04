package app.awaytogo.command.application.handler;

import app.awaytogo.command.domain.command.Command;
import reactor.core.publisher.Mono;

public interface CommandHandler<T extends Command> {
    Mono<String> handle(T command);
}
