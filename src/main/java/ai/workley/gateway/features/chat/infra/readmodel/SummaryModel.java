package ai.workley.gateway.features.chat.infra.readmodel;

public class SummaryModel {
    private String title;

    public static SummaryModel create(String content) {
        return new SummaryModel()
                .setTitle(content);
    }

    public String getTitle() {
        return title;
    }

    public SummaryModel setTitle(String title) {
        this.title = title;
        return this;
    }
}
