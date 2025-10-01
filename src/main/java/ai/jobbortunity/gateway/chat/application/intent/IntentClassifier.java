package ai.jobbortunity.gateway.chat.application.intent;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.service.ClassificationResult;
import reactor.core.publisher.Mono;

public interface IntentClassifier {

    Mono<ClassificationResult> classify(Message<String> message);
}
