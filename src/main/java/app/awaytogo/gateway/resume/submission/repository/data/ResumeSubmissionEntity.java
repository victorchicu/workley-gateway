package app.awaytogo.gateway.resume.submission.repository.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ResumeSubmissionEntity {
    @Id
    private String id;
    private String profileId;

    public String getId() {
        return id;
    }

    public ResumeSubmissionEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getProfileId() {
        return profileId;
    }

    public ResumeSubmissionEntity setProfileId(String profileId) {
        this.profileId = profileId;
        return this;
    }
}
