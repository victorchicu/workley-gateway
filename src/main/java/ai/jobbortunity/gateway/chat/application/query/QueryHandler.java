package ai.jobbortunity.gateway.chat.application.query;

import reactor.core.publisher.Mono;

import java.security.Principal;

public interface QueryHandler<T extends Query, R extends QueryResult> {

    Mono<R> handle(Principal actor, T query);

    Class<T> supported();
}
