package ai.workley.gateway.chat.domain.content;

public record TextContent(String value) implements Content {
    @Override
    public String text() {
        return value;
    }
}
