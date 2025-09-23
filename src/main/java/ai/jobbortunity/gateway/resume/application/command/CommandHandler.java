package ai.jobbortunity.gateway.resume.application.command;

import reactor.core.publisher.Mono;

import java.security.Principal;

public interface CommandHandler<T extends Command, R extends CommandResult> {

    Mono<R> handle(Principal actor, T command);

    Class<T> supported();
}
