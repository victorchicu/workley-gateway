package ai.workley.gateway.features.shared.app.query.handler;

import ai.workley.gateway.features.shared.app.command.results.Output;
import ai.workley.gateway.features.shared.domain.query.Query;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface QueryHandler<T extends Query, R extends Output> {

    Mono<R> handle(Principal actor, T query);

    Class<T> supported();
}
