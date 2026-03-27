package ai.workley.core.chat.model;

import java.util.List;

public record GetChatPayload(String chatId, List<Message<? extends Content>> messages) implements Payload {
}
