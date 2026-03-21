package ai.workley.gateway.chat.model;

public record ReplyChunk(String text) implements Content {

    @Override
    public String type() {
        return "REPLY_CHUNK";
    }
}