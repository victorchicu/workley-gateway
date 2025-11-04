package ai.workley.gateway.features.chat.infra.prompt;

import ai.workley.gateway.features.chat.domain.IntentType;

public record ClassificationResult(IntentType intent, Float confidence) {

    public String getSystemPrompt() {
        return intent.getSystemPrompt(confidence);
    }
}
