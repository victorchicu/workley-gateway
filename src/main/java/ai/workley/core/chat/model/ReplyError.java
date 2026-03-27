package ai.workley.core.chat.model;

public record ReplyError(ErrorCode code, String message) implements Content {
    @Override
    public String type() {
        return "REPLY_ERROR";
    }
}
