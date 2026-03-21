package ai.workley.gateway.chat.model;

import ai.workley.gateway.chat.model.DomainEvent;

public record AggregateCommit<T extends DomainEvent>(T event, long version) {
}
