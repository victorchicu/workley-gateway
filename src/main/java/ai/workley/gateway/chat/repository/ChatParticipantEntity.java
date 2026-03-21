package ai.workley.gateway.chat.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("chat_participants")
public class ChatParticipantEntity {
    @Id
    private Long id;
    @Column("chat_id")
    private String chatId;
    @Column("participant_id")
    private String participantId;

    public Long getId() {
        return id;
    }

    public ChatParticipantEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getChatId() {
        return chatId;
    }

    public ChatParticipantEntity setChatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public String getParticipantId() {
        return participantId;
    }

    public ChatParticipantEntity setParticipantId(String participantId) {
        this.participantId = participantId;
        return this;
    }
}
