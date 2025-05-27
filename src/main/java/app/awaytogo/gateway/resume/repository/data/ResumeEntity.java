package app.awaytogo.gateway.resume.repository.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ResumeEntity {
    @Id
    private String id;
    private String profileId;

    public String getId() {
        return id;
    }

    public ResumeEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getProfileId() {
        return profileId;
    }

    public ResumeEntity setProfileId(String profileId) {
        this.profileId = profileId;
        return this;
    }
}
