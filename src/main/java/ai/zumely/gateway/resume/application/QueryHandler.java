package ai.zumely.gateway.resume.application;

import ai.zumely.gateway.resume.domain.model.ReadModel;
import ai.zumely.gateway.resume.domain.query.Query;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface QueryHandler<Q extends Query, R extends ReadModel> {

    Mono<R> handle(Principal principal, Q query);
}