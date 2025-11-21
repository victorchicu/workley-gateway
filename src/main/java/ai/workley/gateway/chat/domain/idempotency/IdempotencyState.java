package ai.workley.gateway.chat.domain.idempotency;

public enum IdempotencyState {
    PROCESSING, COMPLETED, FAILED
}