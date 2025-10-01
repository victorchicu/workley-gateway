package ai.jobbortunity.gateway.chat.application.service;

import ai.jobbortunity.gateway.chat.application.intent.IntentType;

public record Intent(IntentType type, String reasoning, Float confidence, String offtopic) {

}
