package ai.workley.gateway.chat.domain.payloads;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;

import java.util.List;

public record GetChatPayload(String chatId, List<Message<? extends Content>> messages) implements Payload {
}
