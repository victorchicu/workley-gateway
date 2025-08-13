package io.zumely.gateway.resume.infrastructure.eventstore.data;

public class ParticipantObject {
    private String id;

    public String getId() {
        return id;
    }

    public ParticipantObject setId(String id) {
        this.id = id;
        return this;
    }
}
