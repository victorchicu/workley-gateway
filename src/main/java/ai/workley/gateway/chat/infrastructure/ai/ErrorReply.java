package ai.workley.gateway.chat.infrastructure.ai;

public record ErrorReply(ErrorCode code, String message) implements ReplyEvent {

    @Override
    public String type() {
        return "ERROR";
    }
}