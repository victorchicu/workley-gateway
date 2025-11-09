package ai.workley.gateway.chat.infrastructure.eventstore.mongodb;

import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.infrastructure.eventstore.EventRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MongoEventRepositoryAdapter implements EventRepository {
    private final MongoEventRepository mongoEventRepository;

    public MongoEventRepositoryAdapter(MongoEventRepository mongoEventRepository) {
        this.mongoEventRepository = mongoEventRepository;
    }


    @Override
    public <T extends DomainEvent> Mono<EventDocument<T>> saveEvent(EventDocument<T> eventDocument) {
        return mongoEventRepository.save(eventDocument);
    }

    @Override
    public <T extends DomainEvent> Mono<EventDocument<T>> findLastEvent(String aggregateType, String aggregateId) {
        return mongoEventRepository.findFirstByAggregateTypeAndAggregateIdOrderByVersionDesc(aggregateType, aggregateId);
    }

    @Override
    public <T extends DomainEvent> Flux<EventDocument<T>> findRecentEvents(String aggregateType, String aggregateId) {
        return mongoEventRepository.findAllByAggregateTypeAndAggregateIdOrderByVersionAsc(aggregateType, aggregateId);
    }
}
