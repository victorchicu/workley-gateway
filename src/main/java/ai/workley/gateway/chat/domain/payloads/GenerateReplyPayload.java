package ai.workley.gateway.chat.domain.payloads;

import ai.workley.gateway.chat.domain.Payload;

public record GenerateReplyPayload() implements Payload {
    private static final GenerateReplyPayload EMPTY = new GenerateReplyPayload();

    public static GenerateReplyPayload empty() {
        return EMPTY;
    }
}
