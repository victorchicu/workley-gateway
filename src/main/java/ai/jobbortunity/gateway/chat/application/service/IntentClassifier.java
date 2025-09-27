package ai.jobbortunity.gateway.chat.application.service;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.intent.IntentType;
import reactor.core.publisher.Mono;

public interface IntentClassifier {

    Mono<Intent> classify(Message<String> message);
}
