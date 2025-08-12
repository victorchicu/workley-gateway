package io.zumely.gateway.resume.application.event.handler;

import io.zumely.gateway.resume.application.event.data.MessageAddedApplicationEvent;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.infrastructure.eventstore.ChatStore;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MessageAddedApplicationEventHandler {
    private final ChatStore chatStore;

    public MessageAddedApplicationEventHandler(ChatStore chatStore) {
        this.chatStore = chatStore;
    }

    @EventListener
    public void handle(MessageAddedApplicationEvent source) {
        String actor = source.actor();
        MessageAddedApplicationEvent payload = source;
//        chatStore.exists(actor, payload.chatId())
//                .flatMap((Boolean exists) -> {
//                    if (exists) {
//                        return chatStore.save(actor, payload);
//                    } else {
//                        //It might have been removed, or you may not have the necessary permissions to access it. To share chats on Zumely, use the share button.
//                        return Mono.error(
//                                new ApplicationException("Oops! Chat not found."));
//                    }
//                })
//                .subscribe();
    }
}
