package io.zumely.gateway.resume.application.event;

import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.service.IdGenerator;
import io.zumely.gateway.resume.infrastructure.MessageHistoryRepository;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class GptReplyApplicationEventHandler {
    private final IdGenerator messageIdGenerator;
    private final OpenAiChatModel openAiChatModel;
    private final Sinks.Many<Message<String>> chatSink;
    private final MessageHistoryRepository messageHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public GptReplyApplicationEventHandler(
            IdGenerator messageIdGenerator,
            OpenAiChatModel openAiChatModel,
            Sinks.Many<Message<String>> chatSink,
            MessageHistoryRepository messageHistoryRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.chatSink = chatSink;
        this.openAiChatModel = openAiChatModel;
        this.messageIdGenerator = messageIdGenerator;
        this.messageHistoryRepository = messageHistoryRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    public Mono<Void> handle(GptReplyApplicationEvent source) {
        Prompt prompt = Prompt.builder().content(source.message().content())
                .build();

        throw new UnsupportedOperationException("Not yet implemented");

//        return openAiChatModel.stream(prompt)
//                .map(chatResponse -> {
//                    chatResponse.getResults().get(0).getOutput().getText()
//                })
//                .doOnNext(message -> {
//                    chatSink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
//                })
//                .then();

    }

    private String extractText(ChatResponse chatResponse) {
        return "Oops! Something went wrong.";
    }
}
