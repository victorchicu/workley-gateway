package ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document(collection = "chat_sessions")
public class ChatModel {
    @Id
    private String id;
    @Indexed(unique = true)
    private String chatId;
    private SummaryModel summary;
    private Set<ParticipantModel> participants;
    @CreatedDate
    private Instant createdAt;

    public static ChatModel create(String chatId, SummaryModel summary, Set<ParticipantModel> participants) {
        return new ChatModel()
                .setChatId(chatId)
                .setSummary(summary)
                .setParticipants(participants);
    }

    public String getId() {
        return id;
    }

    public ChatModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getChatId() {
        return chatId;
    }

    public ChatModel setChatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public SummaryModel getSummary() {
        return summary;
    }

    public ChatModel setSummary(SummaryModel summary) {
        this.summary = summary;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ChatModel setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Set<ParticipantModel> getParticipants() {
        return participants;
    }

    public ChatModel setParticipants(Set<ParticipantModel> participants) {
        this.participants = participants;
        return this;
    }
}
