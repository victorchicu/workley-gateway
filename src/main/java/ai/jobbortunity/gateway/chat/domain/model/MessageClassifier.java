package ai.jobbortunity.gateway.chat.domain.model;

import ai.jobbortunity.gateway.chat.application.result.ClassificationResult;
import reactor.core.publisher.Mono;

public interface MessageClassifier {

    Mono<ClassificationResult> classify(Message<String> message);
}
