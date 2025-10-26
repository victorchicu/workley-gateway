package ai.workley.gateway.chat.application.result;

import ai.workley.gateway.chat.domain.model.IntentType;

public record ClassificationResult(IntentType intent, Float confidence) {

}
