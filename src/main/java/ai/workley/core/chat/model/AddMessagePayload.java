package ai.workley.core.chat.model;

public record AddMessagePayload(String chatId, Message<? extends Content> message) implements Payload {

    public static AddMessagePayload ack(String chatId, Message<? extends Content> message) {
        return new AddMessagePayload(chatId, message);
    }
}
