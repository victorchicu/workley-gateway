package ai.workley.gateway.features.chat.infra.readmodel;

public class ParticipantModel {
    private String participantId;

    public static ParticipantModel create(String id) {
        return new ParticipantModel()
                .setParticipantId(id);
    }

    public String getParticipantId() {
        return participantId;
    }

    public ParticipantModel setParticipantId(String participantId) {
        this.participantId = participantId;
        return this;
    }
}
