package ai.workley.core.chat.model;

public record GenerateReplyPayload() implements Payload {
    private static final GenerateReplyPayload EMPTY = new GenerateReplyPayload();

    public static GenerateReplyPayload ack() {
        return EMPTY;
    }
}
