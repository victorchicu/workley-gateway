package ai.workley.gateway.features.chat.infra.component;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.domain.command.results.ClassificationResult;
import reactor.core.publisher.Mono;

public interface IntentClassifier {

    Mono<ClassificationResult> classify(Message<String> message);
}
