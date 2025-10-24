package ai.jobbortunity.gateway.chat.infrastructure.ai;

import ai.jobbortunity.gateway.chat.application.result.ClassificationResult;
import ai.jobbortunity.gateway.chat.domain.model.Message;
import ai.jobbortunity.gateway.chat.domain.model.MessageClassifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Primary
@Service
public class OllamaMessageClassifier implements MessageClassifier {

    @Override
    public Mono<ClassificationResult> classify(Message<String> message) {

        return null;
    }
}