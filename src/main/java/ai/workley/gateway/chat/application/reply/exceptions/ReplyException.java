package ai.workley.gateway.chat.application.reply.exceptions;

import ai.workley.gateway.chat.infrastructure.ai.ErrorCode;

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
