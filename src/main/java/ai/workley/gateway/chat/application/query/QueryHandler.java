package ai.workley.gateway.chat.application.query;

import ai.workley.gateway.chat.domain.Payload;
import ai.workley.gateway.chat.domain.query.Query;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface QueryHandler<T extends Query, R extends Payload> {

    Mono<R> handle(Principal actor, T query);

    Class<T> supported();
}
