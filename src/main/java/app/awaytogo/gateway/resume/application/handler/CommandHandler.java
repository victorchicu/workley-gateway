package app.awaytogo.gateway.resume.application.handler;

import app.awaytogo.gateway.resume.domain.command.Command;
import reactor.core.publisher.Mono;

public interface CommandHandler<T extends Command> {

    Mono<String> handle(T command);
}
