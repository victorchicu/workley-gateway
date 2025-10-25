package ai.workley.gateway.chat.infrastructure.persistent.eventstore;

import ai.workley.gateway.chat.infrastructure.persistent.eventstore.entity.EventModel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends ReactiveMongoRepository<EventModel<?>, String> {

}
