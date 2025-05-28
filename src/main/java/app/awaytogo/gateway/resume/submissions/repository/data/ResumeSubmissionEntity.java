package app.awaytogo.gateway.resume.submissions.repository.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ResumeSubmissionEntity {
    @Id
    private String id;
    private String link;

    public String getId() {
        return id;
    }

    public ResumeSubmissionEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getLink() {
        return link;
    }

    public ResumeSubmissionEntity setLink(String link) {
        this.link = link;
        return this;
    }
}
