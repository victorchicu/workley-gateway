package ai.workley.gateway.chat.model;

import ai.workley.gateway.chat.model.ErrorCode;

public record ReplyError(ErrorCode code, String message) implements Content {
    @Override
    public String type() {
        return "REPLY_ERROR";
    }
}
