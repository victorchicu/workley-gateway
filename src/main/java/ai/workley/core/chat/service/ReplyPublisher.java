package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Message;
import ai.workley.core.chat.model.ReplyChunk;
import ai.workley.core.chat.model.ReplyCompleted;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReplyPublisher {
    private final EventBus eventBus;

    public ReplyPublisher(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Mono<Void> publish(Message<ReplyChunk> message) {
        eventBus.publishEvent(
                new ReplyCompleted(
                        message.ownedBy(), message.chatId(), message));
        return Mono.empty();
    }
}
