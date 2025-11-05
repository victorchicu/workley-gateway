package ai.workley.gateway.features.chat.infra.intent;

import ai.workley.gateway.features.chat.domain.IntentType;

public record IntentClassification(IntentType intent, Float confidence) {

    public String getSystemPrompt() {
        return intent.getSystemPrompt(confidence);
    }
}
