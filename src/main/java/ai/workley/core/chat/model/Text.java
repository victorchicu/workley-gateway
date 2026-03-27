package ai.workley.core.chat.model;

public record Text(String value) implements Content {

    @Override
    public String type() {
        return "TEXT";
    }
}
