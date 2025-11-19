package ai.workley.gateway.chat.domain.content;

public record TextContent(String value) implements Content {

    @Override
    public String type() {
        return "TEXT";
    }
}