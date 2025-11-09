package ai.workley.gateway.chat.infrastructure.ai;

import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Primary
@Component
public class OllamaAiModel implements AiModel {
    private final OllamaChatModel ollamaChatModel;

    public OllamaAiModel(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return ollamaChatModel.stream(prompt);
    }
}
