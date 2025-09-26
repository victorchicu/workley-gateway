package ai.jobbortunity.gateway.chat.infrastructure.data;

public class SummaryObject {
    private String title;

    public static SummaryObject create(String content) {
        return new SummaryObject()
                .setTitle(content);
    }

    public String getTitle() {
        return title;
    }

    public SummaryObject setTitle(String title) {
        this.title = title;
        return this;
    }
}
