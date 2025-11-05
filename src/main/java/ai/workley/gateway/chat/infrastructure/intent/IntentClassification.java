package ai.workley.gateway.chat.infrastructure.intent;

import ai.workley.gateway.chat.domain.IntentType;

public record IntentClassification(IntentType intent, Float confidence) {

    public String getSystemPrompt() {
        return intent.getSystemPrompt(confidence);
    }
}
