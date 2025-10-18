package ai.jobbortunity.gateway.chat.application.handler;

import ai.jobbortunity.gateway.chat.application.command.Command;
import ai.jobbortunity.gateway.chat.application.result.Result;
import reactor.core.publisher.Mono;

public interface CommandHandler<T extends Command, R extends Result> {

    Mono<R> handle(String actor, T command);

    Class<T> supported();
}
