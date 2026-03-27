package ai.workley.core.chat.service;

import ai.workley.core.chat.model.ReplyStarted;
import reactor.core.publisher.Mono;

public interface ReplyFlow {

    Mono<Void> process(ReplyStarted event);
}
