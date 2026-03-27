package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Message;
import ai.workley.core.chat.model.Content;
import ai.workley.core.chat.model.IntentClassification;
import reactor.core.publisher.Mono;

public interface IntentClassifier {

    Mono<IntentClassification> classify(Message<? extends Content> message);
}
