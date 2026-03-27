package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Payload;
import ai.workley.core.chat.model.Command;
import reactor.core.publisher.Mono;

public interface CommandHandler<T extends Command, R extends Payload> {

    Mono<R> handle(String actor, T command);

    Class<T> supported();
}
