package ai.workley.gateway.chat.domain.payloads;

public class ReplyGeneratedPayload implements Payload {
    private static final ReplyGeneratedPayload EMPTY = new ReplyGeneratedPayload();

    public static ReplyGeneratedPayload ack() {
        return EMPTY;
    }
}