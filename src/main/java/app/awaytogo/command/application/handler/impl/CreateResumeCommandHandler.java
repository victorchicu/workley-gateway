package app.awaytogo.command.application.handler.impl;

import app.awaytogo.command.application.handler.CommandHandler;
import app.awaytogo.command.domain.aggregate.ResumeAggregate;
import app.awaytogo.command.domain.command.CreateResumeCommand;
import app.awaytogo.command.domain.event.DomainEvent;
import app.awaytogo.command.infrastructure.eventstore.EventStore;
import app.awaytogo.command.infrastructure.repository.AggregateRepository;
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
                .defaultIfEmpty(new ResumeAggregate())
                .flatMap(aggregate -> {
                    // Aggregate decides what events to generate
                    List<DomainEvent> events = aggregate.handle(command);

                    // Save events atomically
                    return eventStore.saveEvents(aggregate.getId(), events, aggregate.getVersion())
                            .thenReturn(command.getResumeId());
                });
    }
}