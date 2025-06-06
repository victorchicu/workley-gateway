package app.awaytogo.gateway.resume.application.handler.impl;

import app.awaytogo.gateway.resume.application.handler.CommandHandler;
import app.awaytogo.gateway.resume.domain.aggregate.ResumeAggregate;
import app.awaytogo.gateway.resume.domain.command.CreateResumeCommand;
import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import app.awaytogo.gateway.resume.infrastructure.eventstore.EventStore;
import app.awaytogo.gateway.resume.infrastructure.repository.AggregateRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class CreateResumeCommandHandler implements CommandHandler<CreateResumeCommand> {
    private final EventStore eventStore;
    private final AggregateRepository aggregateRepository;

    public CreateResumeCommandHandler(EventStore eventStore, AggregateRepository aggregateRepository) {
        this.eventStore = eventStore;
        this.aggregateRepository = aggregateRepository;
    }

    @Override
    public Mono<String> handle(CreateResumeCommand command) {
        return aggregateRepository.load(command.getResumeId())
                .defaultIfEmpty(
                        new ResumeAggregate()
                                .setResumeId(command.getResumeId())
                )
                .flatMap(aggregate -> {
                    List<DomainEvent> events = aggregate.handle(command);
                    return eventStore.saveEvents(aggregate.getAggregateId(), events, aggregate.getVersion())
                            .thenReturn(command.getResumeId());
                });
    }
}