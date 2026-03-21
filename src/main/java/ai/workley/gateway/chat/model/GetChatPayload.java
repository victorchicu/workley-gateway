package ai.workley.gateway.chat.model;

import ai.workley.gateway.chat.model.Message;
import ai.workley.gateway.chat.model.Content;

import java.util.List;

public record GetChatPayload(String chatId, List<Message<? extends Content>> messages) implements Payload {
}
