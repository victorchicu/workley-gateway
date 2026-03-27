package ai.workley.core.chat.model;

public class ReplyException extends RuntimeException {
    private final ErrorCode code;

    public ReplyException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
