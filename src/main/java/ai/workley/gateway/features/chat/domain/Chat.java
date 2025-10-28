package ai.workley.gateway.features.chat.domain;

import java.util.Set;

public record Chat(String id, Summary summary, Set<Participant> participants) {
    public static Chat create(String id, Summary summary, Set<Participant> participants) {
        return new Chat(id, summary, participants);
    }

    static public class Summary {
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

    static public class Participant {
        private String id;

        public static Participant create(String id) {
            return new Participant()
                    .setId(id);
        }

        public String getId() {
            return id;
        }

        public Participant setId(String id) {
            this.id = id;
            return this;
        }
    }
}
