package ai.workley.gateway.chat.application.ports.outbound.intent;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.intent.IntentClassification;
import reactor.core.publisher.Mono;

public interface IntentClassifier {

    Mono<IntentClassification> classify(Message<String> message);
}
