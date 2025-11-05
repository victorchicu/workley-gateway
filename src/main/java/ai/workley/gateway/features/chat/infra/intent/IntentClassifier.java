package ai.workley.gateway.features.chat.infra.intent;

import ai.workley.gateway.features.chat.domain.Message;
import reactor.core.publisher.Mono;

public interface IntentClassifier {

    Mono<IntentClassification> classify(Message<String> message);
}
