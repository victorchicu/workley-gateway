package ai.workley.gateway.chat.model;

import ai.workley.gateway.chat.model.IntentType;

public record IntentClassification(IntentType intent, Float confidence) {

    public String getSystemPrompt() {
        return intent.getSystemPrompt(confidence);
    }
}
