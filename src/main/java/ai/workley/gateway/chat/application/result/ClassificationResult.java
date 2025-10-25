package ai.workley.gateway.chat.application.result;

import ai.workley.gateway.chat.domain.model.IntentType;

public record ClassificationResult(IntentType intent, String reasoning, Float confidence, String unrelated) implements Result {

}
