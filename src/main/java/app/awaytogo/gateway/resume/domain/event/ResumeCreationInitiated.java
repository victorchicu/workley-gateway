package app.awaytogo.gateway.resume.domain.event;

import java.time.Instant;

public class ResumeCreationInitiated implements DomainEvent {
    String resumeId;
    String userId;
    String linkedinUrl;
    Instant timestamp;

    public ResumeCreationInitiated(String resumeId, String userId, String linkedinUrl, Instant timestamp) {
        this.resumeId = resumeId;
        this.userId = userId;
        this.linkedinUrl = linkedinUrl;
        this.timestamp = timestamp;
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
