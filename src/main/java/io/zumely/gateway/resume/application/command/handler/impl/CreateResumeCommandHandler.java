package io.zumely.gateway.resume.application.command.handler.impl;

import io.zumely.gateway.core.service.AggregateIdGenerator;
import io.zumely.gateway.resume.application.command.handler.CommandHandler;
import io.zumely.gateway.resume.application.command.impl.CreateResumeCommand;
import io.zumely.gateway.resume.application.command.result.impl.CreateResumeResult;
import io.zumely.gateway.resume.application.event.impl.CreateResumeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CreateResumeCommandHandler implements CommandHandler<CreateResumeCommand, CreateResumeResult> {
    private final AggregateIdGenerator aggregateIdGenerator;;
    private final ApplicationEventPublisher applicationEventPublisher;

    public CreateResumeCommandHandler(
            AggregateIdGenerator aggregateIdGenerator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.aggregateIdGenerator = aggregateIdGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public CreateResumeResult handle(CreateResumeCommand command) {

        String aggregateId = aggregateIdGenerator.generate();

        applicationEventPublisher.publishEvent(new CreateResumeEvent(aggregateId, command.prompt()));

        return new CreateResumeResult(aggregateId);
    }

    @Override
    public Class<CreateResumeCommand> supported() {
        return CreateResumeCommand.class;
    }
}
