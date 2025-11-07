package ai.workley.gateway.chat.domain.aggregations;

import ai.workley.gateway.chat.domain.events.DomainEvent;

public record AggregateCommit<T extends DomainEvent>(T event, long version) {
}
