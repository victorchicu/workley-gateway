package ai.jobbortunity.gateway.chat.application.event.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class IdentifyIntentProjection {
    private static final Logger log = LoggerFactory.getLogger(IdentifyIntentProjection.class);

    private final OpenAiChatModel openAiChatModel;

    public IdentifyIntentProjection(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    @EventListener
    public Mono<Void> handle(IdentifyIntentEvent e) {
        return Mono.defer(Mono::empty)
                .doOnError(error ->
                        log.error("Failed to identify intent (actor={}, chatId={}, message={})",
                                e.actor(), e.chatId(), e.message().content(), error))
                .onErrorResume(error -> Mono.empty())
                .then();
    }
}