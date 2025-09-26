package ai.jobbortunity.gateway.chat.application.command;

import reactor.core.publisher.Mono;

import java.security.Principal;

public interface CommandHandler<T extends Command, R extends CommandResult> {

    Mono<R> handle(String actor, T command);

    Class<T> supported();
}
