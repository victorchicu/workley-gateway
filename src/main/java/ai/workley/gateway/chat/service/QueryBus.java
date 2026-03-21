package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.Payload;
import ai.workley.gateway.chat.model.Query;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface QueryBus {

    <T extends Query, R extends Payload> Mono<R> execute(Principal actor, T query);
}