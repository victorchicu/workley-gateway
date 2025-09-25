package ai.jobbortunity.gateway.chat.infrastructure.data;

public class ParticipantObject {
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
