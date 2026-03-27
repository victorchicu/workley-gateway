package ai.workley.core.chat.service;

import ai.workley.core.chat.model.ReplyEvent;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AiModel {

    Mono<ReplyEvent> call(Prompt prompt);

    Flux<ReplyEvent> stream(Prompt prompt);
}
