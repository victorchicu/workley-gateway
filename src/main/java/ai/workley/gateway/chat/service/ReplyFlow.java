package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.ReplyStarted;
import reactor.core.publisher.Mono;

public interface ReplyFlow {

    Mono<Void> process(ReplyStarted event);
}
