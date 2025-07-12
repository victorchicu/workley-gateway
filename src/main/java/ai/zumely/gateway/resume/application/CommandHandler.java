package ai.zumely.gateway.resume.application;

import ai.zumely.gateway.resume.domain.command.Command;
import ai.zumely.gateway.resume.domain.command.Response;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface CommandHandler<T extends Command, R extends Response> {

    Mono<R> handle(Principal principal, T command);
}
