package ai.workley.gateway.chat.domain.content;

public record Text(String value) implements Content {

    @Override
    public String type() {
        return "TEXT";
    }
}
