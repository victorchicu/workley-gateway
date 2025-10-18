package ai.jobbortunity.gateway.chat.application.query;

import ai.jobbortunity.gateway.chat.application.result.Result;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface QueryHandler<T extends Query, R extends Result> {

    Mono<R> handle(Principal actor, T query);

    Class<T> supported();
}
