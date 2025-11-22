package ai.workley.gateway.chat.infrastructure.ai;

public record ChunkReply(String text) implements ReplyEvent {
    @Override
    public String type() {
        return "TEXT_CHUNK";
    }
}