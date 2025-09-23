package io.zumely.gateway.resume.application.command.impl;

import io.zumely.gateway.resume.application.command.CommandHandler;
import io.zumely.gateway.resume.application.event.impl.EmbeddingSavedApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class SaveEmbeddingCommandHandler implements CommandHandler<SaveEmbeddingCommand, SaveEmbeddingCommandResult> {
    private final ApplicationEventPublisher applicationEventPublisher;

    public SaveEmbeddingCommandHandler(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<SaveEmbeddingCommand> supported() {
        return SaveEmbeddingCommand.class;
    }

    @Override
    public Mono<SaveEmbeddingCommandResult> handle(Principal actor, SaveEmbeddingCommand command) {
        applicationEventPublisher.publishEvent(new EmbeddingSavedApplicationEvent(actor, command.message()));
        return Mono.empty();
    }
}