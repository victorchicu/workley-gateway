package ai.jobbortunity.gateway.resume.infrastructure.data;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document(collection = "chat_sessions")
public class ChatObject {
    @Id
    private String id;
    private String chatId;
    private Set<ParticipantObject> participants;
    private SummaryObject<MessageObject<String>> summary;
    @CreatedDate
    private Instant createdAt;

    public static ChatObject create(String chatId, SummaryObject<MessageObject<String>> summary, Set<ParticipantObject> participants) {
        return new ChatObject()
                .setChatId(chatId)
                .setSummary(summary)
                .setParticipants(participants);
    }

    public String getId() {
        return id;
    }

    public ChatObject setId(String id) {
        this.id = id;
        return this;
    }

    public String getChatId() {
        return chatId;
    }

    public ChatObject setChatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ChatObject setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public SummaryObject<MessageObject<String>> getSummary() {
        return summary;
    }

    public ChatObject setSummary(SummaryObject<MessageObject<String>> summary) {
        this.summary = summary;
        return this;
    }

    public Set<ParticipantObject> getParticipants() {
        return participants;
    }

    public ChatObject setParticipants(Set<ParticipantObject> participants) {
        this.participants = participants;
        return this;
    }
}
