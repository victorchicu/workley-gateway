package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.Command;
import ai.workley.gateway.chat.model.Payload;
import reactor.core.publisher.Mono;

public interface CommandBus {

    <T extends Command, R extends Payload> Mono<R> execute(String actor, T command);

    <T extends Command, R extends Payload> Mono<R> execute(String actor, T command, String idempotencyKey);
}
