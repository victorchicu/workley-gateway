package ai.workley.gateway.chat.application.reply.core;

import ai.workley.gateway.chat.application.ports.outbound.bus.EventBus;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.TextContent;
import ai.workley.gateway.chat.domain.events.ReplyCompleted;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReplyPublisher {
    private final EventBus eventBus;

    public ReplyPublisher(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Mono<Void> publish(Message<TextContent> message) {
        eventBus.publishEvent(
                new ReplyCompleted(
                        message.ownedBy(), message.chatId(), message));
        return Mono.empty();
    }
}
