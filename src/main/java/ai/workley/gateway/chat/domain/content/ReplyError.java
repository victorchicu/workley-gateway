package ai.workley.gateway.chat.domain.content;

import ai.workley.gateway.chat.infrastructure.ai.ErrorCode;

public record ReplyError(ErrorCode code, String message) implements Content {
    @Override
    public String type() {
        return "REPLY_ERROR";
    }
}
