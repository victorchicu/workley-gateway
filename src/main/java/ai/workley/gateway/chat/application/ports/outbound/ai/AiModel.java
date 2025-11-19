package ai.workley.gateway.chat.application.ports.outbound.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiModel {
    Logger log = LoggerFactory.getLogger(AiModel.class);

    Flux<ChatResponse> stream(Prompt prompt);

    default Flux<ChatResponse> underHeavyLoad(Throwable throwable) {
        log.error("AI model under heavy load", throwable);
        return Flux.just(
                new ChatResponse(
                        List.of(new Generation(new AssistantMessage("Workley is currently under heavy load. Please try again later."))))
        );
    }
}