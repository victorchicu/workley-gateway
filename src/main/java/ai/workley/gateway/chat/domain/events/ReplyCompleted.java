package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;

public record ReplyCompleted(String actor, String chatId, Message<? extends Content> message) implements DomainEvent {

}