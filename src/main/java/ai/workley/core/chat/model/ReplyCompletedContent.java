package ai.workley.core.chat.model;

public record ReplyCompletedContent(String text) implements Content {

    @Override
    public String type() {
        return "REPLY_COMPLETED";
    }
}
