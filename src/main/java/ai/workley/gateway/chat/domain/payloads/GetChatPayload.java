package ai.workley.gateway.chat.domain.payloads;

import ai.workley.gateway.chat.domain.Message;

import java.util.List;

public record GetChatPayload(String chatId, List<Message<String>> messages) implements Payload {
}
