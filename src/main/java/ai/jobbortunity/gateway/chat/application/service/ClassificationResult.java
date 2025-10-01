package ai.jobbortunity.gateway.chat.application.service;

import ai.jobbortunity.gateway.chat.application.intent.IntentType;

public record ClassificationResult(IntentType intent, String reasoning, Float confidence, String unrelated) {

}
