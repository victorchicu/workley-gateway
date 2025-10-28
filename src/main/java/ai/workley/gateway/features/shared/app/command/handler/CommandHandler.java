package ai.workley.gateway.features.shared.app.command.handler;

import ai.workley.gateway.features.shared.app.command.results.Output;
import ai.workley.gateway.features.shared.domain.command.Command;
import reactor.core.publisher.Mono;

public interface CommandHandler<T extends Command, R extends Output> {

    Mono<R> handle(String actor, T command);

    Class<T> supported();
}
