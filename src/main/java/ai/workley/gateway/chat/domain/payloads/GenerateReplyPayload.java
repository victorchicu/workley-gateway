package ai.workley.gateway.chat.domain.payloads;

public record GenerateReplyPayload() implements Payload {
    private static final GenerateReplyPayload EMPTY = new GenerateReplyPayload();

    public static GenerateReplyPayload empty() {
        return EMPTY;
    }
}
