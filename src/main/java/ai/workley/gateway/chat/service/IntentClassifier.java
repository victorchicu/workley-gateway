package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.Message;
import ai.workley.gateway.chat.model.Content;
import ai.workley.gateway.chat.model.IntentClassification;
import reactor.core.publisher.Mono;

public interface IntentClassifier {

    Mono<IntentClassification> classify(Message<? extends Content> message);
}
