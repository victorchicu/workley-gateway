package ai.jobbortunity.gateway.chat.infrastructure.data;

import org.springframework.data.mongodb.core.index.Indexed;

public class ParticipantObject {
    @Indexed(unique = true)
    private String participantId;

    public static ParticipantObject create(String id) {
        return new ParticipantObject()
                .setParticipantId(id);
    }

    public String getParticipantId() {
        return participantId;
    }

    public ParticipantObject setParticipantId(String participantId) {
        this.participantId = participantId;
        return this;
    }
}
