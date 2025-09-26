package ai.jobbortunity.gateway.chat.application.service.impl;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.intent.Intent;
import ai.jobbortunity.gateway.chat.application.service.IntentClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OpenAiIntentClassifier implements IntentClassifier {
    private static final Logger log = LoggerFactory.getLogger(OpenAiIntentClassifier.class);

    private final OpenAiChatModel openAiChatModel;

    public OpenAiIntentClassifier(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    public Mono<Intent> classify(Message<String> message) {

        throw new UnsupportedOperationException();
    }
}
