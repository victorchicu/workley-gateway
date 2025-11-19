package ai.workley.gateway.chat.domain.content;

import ai.workley.gateway.chat.infrastructure.ai.ErrorCode;

public record ErrorContent(ErrorCode code) implements Content {
    @Override
    public String type() {
        return "ERROR";
    }
}
