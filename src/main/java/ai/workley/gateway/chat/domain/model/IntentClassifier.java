package ai.workley.gateway.chat.domain.model;

import ai.workley.gateway.chat.application.result.ClassificationResult;
import reactor.core.publisher.Mono;

public interface IntentClassifier {

    Mono<ClassificationResult> classify(Message<String> message);
}
