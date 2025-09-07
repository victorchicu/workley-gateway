package io.zumely.gateway.resume.application.command;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class AskGptCommandHandler implements CommandHandler<AskGptCommand, AskGptCommandResult> {
    @Override
    public Mono<AskGptCommandResult> handle(Principal actor, AskGptCommand command) {
        return null;
    }

    @Override
    public Class<AskGptCommand> supported() {
        return null;
    }
}
