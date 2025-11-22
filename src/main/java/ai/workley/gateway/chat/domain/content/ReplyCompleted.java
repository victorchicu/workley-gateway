package ai.workley.gateway.chat.domain.content;

public record ReplyCompleted(String text) implements Content {

    @Override
    public String type() {
        return "REPLY_COMPLETED";
    }
}