package ai.jobbortunity.gateway.chat.application.intent;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.service.Intent;
import reactor.core.publisher.Mono;

public interface IntentClassifier {

    Mono<Intent> classify(Message<String> message);
}
