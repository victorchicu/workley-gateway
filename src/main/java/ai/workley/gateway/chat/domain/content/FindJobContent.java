package ai.workley.gateway.chat.domain.content;

public record FindJobContent(String text) implements Content {
    @Override
    public String text() {
        return text;
    }
}