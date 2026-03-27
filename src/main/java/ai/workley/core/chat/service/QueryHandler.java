package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Payload;
import ai.workley.core.chat.model.Query;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface QueryHandler<T extends Query, R extends Payload> {

    Mono<R> handle(Principal actor, T query);

    Class<T> supported();
}
