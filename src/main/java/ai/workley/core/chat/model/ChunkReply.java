package ai.workley.core.chat.model;

public record ChunkReply(String text) implements ReplyEvent {
    @Override
    public String type() {
        return "TEXT_CHUNK";
    }
}
