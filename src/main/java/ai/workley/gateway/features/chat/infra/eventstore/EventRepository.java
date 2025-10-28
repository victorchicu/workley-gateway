package ai.workley.gateway.features.chat.infra.eventstore;

import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.EventDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends ReactiveMongoRepository<EventDocument<?>, String> {

}
