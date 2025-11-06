package ai.workley.gateway.chat.application.sagas;

import ai.workley.gateway.chat.domain.events.ReplyFailed;
import ai.workley.gateway.chat.domain.events.ReplyGenerated;
import ai.workley.gateway.chat.domain.events.ReplyInitiated;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReplySaga {

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyFailed e) {
        throw new UnsupportedOperationException();
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyInitiated e) {
        throw new UnsupportedOperationException();
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyGenerated e) {
        throw new UnsupportedOperationException();
    }
}