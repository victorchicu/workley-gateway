package ai.workley.gateway.features.chat.infra.persistent.mongodb.document;

public class Summary {
    private String title;

    public static Summary create(String content) {
        return new Summary()
                .setTitle(content);
    }

    public String getTitle() {
        return title;
    }

    public Summary setTitle(String title) {
        this.title = title;
        return this;
    }
}
