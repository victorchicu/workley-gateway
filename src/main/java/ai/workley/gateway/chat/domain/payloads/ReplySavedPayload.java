package ai.workley.gateway.chat.domain.payloads;

public class ReplySavedPayload implements Payload {
    private static final ReplySavedPayload EMPTY = new ReplySavedPayload();

    public static ReplySavedPayload ack() {
        return EMPTY;
    }
}
