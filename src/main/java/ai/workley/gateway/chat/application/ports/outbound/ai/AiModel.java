package ai.workley.gateway.chat.application.ports.outbound.ai;

import ai.workley.gateway.chat.infrastructure.ai.ReplyEvent;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public interface AiModel {

    Flux<ReplyEvent> stream(Prompt prompt);
}