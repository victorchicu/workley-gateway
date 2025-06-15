package app.awaytogo.gateway.resume.application;

import app.awaytogo.gateway.resume.domain.command.Command;
import app.awaytogo.gateway.resume.domain.command.Response;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface CommandHandler<T extends Command, R extends Response> {

    Mono<R> handle(Principal principal, T command);
}
