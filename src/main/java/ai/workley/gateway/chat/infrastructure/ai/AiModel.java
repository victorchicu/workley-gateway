package ai.workley.gateway.chat.infrastructure.ai;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public interface AiModel {

    Flux<ChatResponse> stream(Prompt prompt);
}
