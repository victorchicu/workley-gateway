package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.command.impl.AddChatMessageCommand;
import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.command.Role;
import io.zumely.gateway.resume.application.service.IdGenerator;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.function.Predicate;

@Component
public class AssistantReplyAddedApplicationEventHandler {
    private final IdGenerator messageIdGenerator;
    private final OpenAiChatModel openAiChatModel;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AssistantReplyAddedApplicationEventHandler(
            IdGenerator messageIdGenerator,
            OpenAiChatModel openAiChatModel,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.openAiChatModel = openAiChatModel;
        this.messageIdGenerator = messageIdGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    public Mono<Void> handle(AssistantReplyAddedApplicationEvent source) {
        Prompt prompt = Prompt.builder()
                .content(source.prompt())
                .build();
        return Flux.from(openAiChatModel.stream(prompt))
                .flatMapIterable(ChatResponse::getResults)
                .map((Generation generation) -> {
                    if (generation == null)
                        return "";
                    String text = generation.getOutput().getText();
                    return text != null ? text : "";
                })
                .filter(Predicate.not(String::isBlank))
                .map((String text) -> toMessage(source, text))
                .concatMap((Message<String> savedCandidate) ->
                        Mono.fromRunnable(() ->
                                applicationEventPublisher.publishEvent(
                                        new AddChatMessageCommand(source.chatId(), savedCandidate)
                                )
                        )
                )
                .then();

    }

    private Message<String> toMessage(AssistantReplyAddedApplicationEvent source, String text) {
        return Message.create(messageIdGenerator.generate(), source.chatId(), source.actor().getName(), Role.ASSISTANT, Instant.now(), text);
    }
}
