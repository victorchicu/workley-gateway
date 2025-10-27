package ai.workley.gateway.features.shared.app.command;

import ai.workley.gateway.features.shared.domain.command.Command;
import ai.workley.gateway.features.shared.app.command.results.Result;
import reactor.core.publisher.Mono;

public interface CommandHandler<T extends Command, R extends Result> {

    Mono<R> handle(String actor, T command);

    Class<T> supported();
}
