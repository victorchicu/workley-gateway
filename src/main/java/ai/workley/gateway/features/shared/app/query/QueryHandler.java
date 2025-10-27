package ai.workley.gateway.features.shared.app.query;

import ai.workley.gateway.features.shared.domain.query.Query;
import ai.workley.gateway.features.shared.app.command.results.Result;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface QueryHandler<T extends Query, R extends Result> {

    Mono<R> handle(Principal actor, T query);

    Class<T> supported();
}
