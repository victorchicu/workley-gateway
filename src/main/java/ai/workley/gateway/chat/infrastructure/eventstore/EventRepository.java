package ai.workley.gateway.chat.infrastructure.eventstore;

import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EventDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends ReactiveMongoRepository<EventDocument<?>, String> {

}
