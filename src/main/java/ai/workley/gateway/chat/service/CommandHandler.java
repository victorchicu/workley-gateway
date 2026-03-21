package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.Payload;
import ai.workley.gateway.chat.model.Command;
import reactor.core.publisher.Mono;

public interface CommandHandler<T extends Command, R extends Payload> {

    Mono<R> handle(String actor, T command);

    Mono<R> handle(String actor, T command, String idempotencyKey);

    Class<T> supported();
}