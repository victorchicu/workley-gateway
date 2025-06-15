package app.awaytogo.gateway.resume.application;

import app.awaytogo.gateway.resume.domain.model.ReadModel;
import app.awaytogo.gateway.resume.domain.query.Query;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface QueryHandler<Q extends Query, R extends ReadModel> {

    Mono<R> handle(Principal principal, Q query);
}