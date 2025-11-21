package ai.workley.gateway.chat.application.command;

import ai.workley.gateway.chat.domain.payloads.Payload;
import ai.workley.gateway.chat.domain.command.Command;
import reactor.core.publisher.Mono;

public interface CommandHandler<T extends Command, R extends Payload> {

    Mono<R> handle(String actor, T command);

    Mono<R> handle(String actor, T command, String idempotencyKey);

    Class<T> supported();
}
