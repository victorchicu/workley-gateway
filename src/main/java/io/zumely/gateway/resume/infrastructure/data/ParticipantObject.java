package io.zumely.gateway.resume.infrastructure.data;

public class ParticipantObject {
    private String id;

    public static ParticipantObject create(String id) {
        return new ParticipantObject()
                .setId(id);
    }

    public String getId() {
        return id;
    }

    public ParticipantObject setId(String id) {
        this.id = id;
        return this;
    }
}
