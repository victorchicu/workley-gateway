package app.awaytogo.gateway.resume.domain.command;

import java.time.Instant;

public class CreateResumeCommand implements Command {
    String resumeId;
    String userId;
    String linkedinUrl;
    Instant timestamp;

    private CreateResumeCommand(Builder builder) {
        resumeId = builder.resumeId;
        userId = builder.userId;
        linkedinUrl = builder.linkedinUrl;
        timestamp = builder.timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getResumeId() {
        return resumeId;
    }

    public String getUserId() {
        return userId;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public static final class Builder {
        private String resumeId;
        private String userId;
        private String linkedinUrl;
        private Instant timestamp;

        private Builder() {
        }

        public Builder resumeId(String resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder linkedinUrl(String linkedinUrl) {
            this.linkedinUrl = linkedinUrl;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public CreateResumeCommand build() {
            return new CreateResumeCommand(this);
        }
    }
}
