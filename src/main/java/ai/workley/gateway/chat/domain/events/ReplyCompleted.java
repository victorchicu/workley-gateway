package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.Message;

public record ReplyCompleted(String actor, String chatId, Message<String> message) implements DomainEvent {

}