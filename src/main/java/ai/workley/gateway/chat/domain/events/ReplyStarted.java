package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.TextContent;

public record ReplyStarted(String actor, String chatId, Message<TextContent> message) implements DomainEvent {

}
