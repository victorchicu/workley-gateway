package ai.workley.gateway.chat.application.ports.inbound;

import ai.workley.gateway.chat.domain.payloads.Payload;
import ai.workley.gateway.chat.domain.query.Query;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface QueryBus {

    <T extends Query, R extends Payload> Mono<R> execute(Principal actor, T query);
}