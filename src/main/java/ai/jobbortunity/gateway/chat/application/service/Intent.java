package ai.jobbortunity.gateway.chat.application.service;

import ai.jobbortunity.gateway.chat.application.intent.IntentType;

public record Intent(IntentType intentType, String reasoning, Float confidence, String offtopic) {

}
