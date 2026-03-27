package ai.workley.core.chat.model;

public record IntentClassification(IntentType intent, Float confidence) {

    public String getSystemPrompt() {
        return intent.getSystemPrompt(confidence);
    }
}
