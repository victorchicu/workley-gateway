package ai.workley.gateway.chat.application.handler;

import ai.workley.gateway.chat.application.command.Command;
import ai.workley.gateway.chat.application.result.Result;
import reactor.core.publisher.Mono;

public interface CommandHandler<T extends Command, R extends Result> {

    Mono<R> handle(String actor, T command);

    Class<T> supported();
}
