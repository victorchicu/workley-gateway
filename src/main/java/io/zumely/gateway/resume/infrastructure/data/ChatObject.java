package io.zumely.gateway.resume.infrastructure.data;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document(collection = "chat_sessions")
public class ChatObject {
    private String id;
    private SummaryObject<String> summary;
    private Set<ParticipantObject> participants;
    @CreatedDate
    private Instant createdAt;

    public static ChatObject create(String chatId, SummaryObject<String> summary, Set<ParticipantObject> participants) {
        return new ChatObject()
                .setId(chatId)
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ChatObject setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public SummaryObject<String> getSummary() {
        return summary;
    }

    public ChatObject setSummary(SummaryObject<String> summary) {
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