package app.awaytogo.gateway.resume.domain;

import java.security.Principal;
import java.util.List;

public class ResumeAggregate {
    private ResumeId resumeId;
    private Principal principal;
    private String linkedinUrl;

    private List<DomainEvent> uncommittedEvents;

    public ResumeAggregate(ResumeId resumeId, Principal principal, String linkedinUrl) {
        this.resumeId = resumeId;
        this.principal = principal;
        this.linkedinUrl = linkedinUrl;
    }

    public static ResumeAggregate create(ResumeId resumeId, Principal principal, String linkedinUrl) {
        return new ResumeAggregate(resumeId, principal, linkedinUrl);
    }

    public ResumeId getResumeId() {
        return resumeId;
    }

    public void setResumeId(ResumeId resumeId) {
        this.resumeId = resumeId;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public List<DomainEvent> getUncommittedEvents() {
        return uncommittedEvents;
    }

    public void setUncommittedEvents(List<DomainEvent> uncommittedEvents) {
        this.uncommittedEvents = uncommittedEvents;
    }

    public void markEventsAsCommitted() {

    }
}
