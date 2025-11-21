package ai.workley.gateway.chat.application.ports.inbound;

import ai.workley.gateway.chat.domain.command.Command;
import ai.workley.gateway.chat.domain.payloads.Payload;
import reactor.core.publisher.Mono;

public interface CommandBus {

    <T extends Command, R extends Payload> Mono<R> execute(String actor, T command);

    <T extends Command, R extends Payload> Mono<R> execute(String actor, T command, String idempotencyKey);
}
