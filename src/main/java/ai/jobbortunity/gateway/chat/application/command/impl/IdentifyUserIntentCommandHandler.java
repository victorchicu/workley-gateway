package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class IdentifyUserIntentCommandHandler implements CommandHandler<IdentifyUserIntentCommand, IdentifyUserIntentCommandResult> {
    @Override
    public Class<IdentifyUserIntentCommand> supported() {
        return IdentifyUserIntentCommand.class;
    }

    @Override
    public Mono<IdentifyUserIntentCommandResult> handle(String actor, IdentifyUserIntentCommand command) {

        return Mono.just(IdentifyUserIntentCommandResult.empty());
    }
}
