package ai.workley.gateway.chat.application.reply;

import ai.workley.gateway.chat.domain.events.ReplyStarted;
import reactor.core.publisher.Mono;

public interface ReplyFlow {

    Mono<Void> process(ReplyStarted event);
}
