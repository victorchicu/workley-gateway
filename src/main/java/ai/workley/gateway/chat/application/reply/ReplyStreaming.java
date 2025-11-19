package ai.workley.gateway.chat.application.reply;

import ai.workley.gateway.chat.domain.events.ReplyStarted;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReplyStreaming {
    private final ReplyFlow replyFlow;

    public ReplyStreaming(ReplyFlow replyFlow) {
        this.replyFlow = replyFlow;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyStarted e) {
        return replyFlow.process(e);
    }
}