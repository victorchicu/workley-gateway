package app.awaytogo.gateway.resume.domain.event;

import java.time.Instant;

public class ResumeCreationInitiated implements DomainEvent {

    String resumeId;
    Instant timestamp;

    public ResumeCreationInitiated(String resumeId, Instant timestamp) {
        this.resumeId = resumeId;
        this.timestamp = timestamp;
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
