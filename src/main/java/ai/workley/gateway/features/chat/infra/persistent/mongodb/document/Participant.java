package ai.workley.gateway.features.chat.infra.persistent.mongodb.document;

public class Participant {
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
