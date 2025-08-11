package io.zumely.gateway.resume.application.query.handler;

import io.zumely.gateway.resume.application.query.Query;
import io.zumely.gateway.resume.application.query.data.QueryResult;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface QueryHandler<T extends Query, R extends QueryResult> {

    Mono<R> handle(Principal actor, T query);

    Class<T> supported();
}
