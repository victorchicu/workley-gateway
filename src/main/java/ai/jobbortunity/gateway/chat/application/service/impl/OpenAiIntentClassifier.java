package ai.jobbortunity.gateway.chat.application.service.impl;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.intent.IntentType;
import ai.jobbortunity.gateway.chat.application.service.Intent;
import ai.jobbortunity.gateway.chat.application.service.IntentClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OpenAiIntentClassifier implements IntentClassifier {
    private static final Logger log = LoggerFactory.getLogger(OpenAiIntentClassifier.class);

    private static final String SYSTEM_PROMPT = """
        You are an intent classifier for Jobbortunity job search platform.
        Classify the user message into one of these categories:
        
        - JOB_SEARCH: User is looking for a job
        - CANDIDATE_SEARCH: User is looking to hire someone
        - OTHER: Not related to jobs or hiring
        
        Respond with ONLY the intent type, nothing else.
        
        Examples:
        "I want to work as a Java developer" → JOB_SEARCH
        "Looking for a Python engineer" → CANDIDATE_SEARCH
        "What's the weather?" → OTHER
        """;

    private final OpenAiChatModel openAiChatModel;

    public OpenAiIntentClassifier(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    @Override
    public Mono<Intent> classify(Message<String> message) {
        log.debug("Classifying intent for: {}", message.content());

        SystemMessage systemMessage = new SystemMessage(SYSTEM_PROMPT);
        UserMessage userMessage = new UserMessage(message.content());

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.1)
                .maxTokens(10)
                .build();

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);

        return openAiChatModel.stream(prompt)
                .mapNotNull(response -> response.getResult().getOutput().getText())
                .filter(content -> content != null && !content.isEmpty())
                .reduce("", (accumulator, chunk) -> accumulator + chunk)
                .map(String::trim)
                .map(this::parseIntentType)
                .map(Intent::new)
                .doOnSuccess(intent -> log.info("Classified as: {}", intent.type()))
                .onErrorResume(error -> {
                    log.error("Classification failed", error);
                    return Mono.just(new Intent(IntentType.OTHER));
                });
    }

    private IntentType parseIntentType(String response) {
        try {
            return IntentType.valueOf(response.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown intent type: {}", response);
            return IntentType.OTHER;
        }
    }
}
