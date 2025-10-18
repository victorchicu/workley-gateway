package ai.jobbortunity.gateway.chat.application.result;

import ai.jobbortunity.gateway.chat.domain.model.IntentType;

public record ClassificationResult(IntentType intent, String reasoning, Float confidence, String unrelated) {

}
