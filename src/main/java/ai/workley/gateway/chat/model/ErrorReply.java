package ai.workley.gateway.chat.model;

public record ErrorReply(ErrorCode code, String message) implements ReplyEvent {

    @Override
    public String type() {
        return "ERROR_REPLY";
    }
}