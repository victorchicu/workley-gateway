package ai.workley.core.chat.model;

public record AggregateCommit<T extends DomainEvent>(T event, long version) {
}
