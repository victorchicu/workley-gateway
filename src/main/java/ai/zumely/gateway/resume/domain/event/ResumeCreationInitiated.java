package ai.zumely.gateway.resume.domain.event;

import java.time.Instant;

public class ResumeCreationInitiated implements DomainEvent {

    String resumeId;
    Instant createdOn;

    public ResumeCreationInitiated(String resumeId, Instant createdOn) {
        this.resumeId = resumeId;
        this.createdOn = createdOn;
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }
}
