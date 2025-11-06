package ai.workley.gateway.chat.infrastructure.exceptions;

public class ConcurrencyException extends RuntimeException {
    public ConcurrencyException(String aggregateType, String aggregateId, Long expectedVersion, Long actualVersion) {
        super(String.format("Aggregate %s[%s] expected version %s but was %s", aggregateType, aggregateId, expectedVersion, actualVersion));
    }
}
