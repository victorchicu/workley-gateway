package ai.workley.gateway.features.chat.infra.persistent.mongodb.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document(collection = "chat_sessions")
public class ChatDocument {
    @Id
    private String id;
    @Indexed(unique = true)
    private String chatId;
    private Summary summary;
    private Set<Participant> participants;
    @CreatedDate
    private Instant createdAt;

    public static ChatDocument create(String chatId, Summary summary, Set<Participant> participants) {
        return new ChatDocument()
                .setChatId(chatId)
                .setSummary(summary)
                .setParticipants(participants);
    }

    public String getId() {
        return id;
    }

    public ChatDocument setId(String id) {
        this.id = id;
        return this;
    }

    public String getChatId() {
        return chatId;
    }

    public ChatDocument setChatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public Summary getSummary() {
        return summary;
    }

    public ChatDocument setSummary(Summary summary) {
        this.summary = summary;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ChatDocument setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Set<Participant> getParticipants() {
        return participants;
    }

    public ChatDocument setParticipants(Set<Participant> participants) {
        this.participants = participants;
        return this;
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
        private String participantId;

        public static Participant create(String id) {
            return new Participant()
                    .setParticipantId(id);
        }

        public String getParticipantId() {
            return participantId;
        }

        public Participant setParticipantId(String participantId) {
            this.participantId = participantId;
            return this;
        }
    }
}
