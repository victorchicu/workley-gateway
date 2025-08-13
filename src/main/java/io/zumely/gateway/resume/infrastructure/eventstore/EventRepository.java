package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.infrastructure.data.EventObject;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends ReactiveMongoRepository<EventObject<?>, String> {

}
