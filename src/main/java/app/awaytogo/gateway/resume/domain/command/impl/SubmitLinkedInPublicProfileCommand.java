package app.awaytogo.gateway.resume.domain.command.impl;

import app.awaytogo.gateway.resume.domain.command.Command;

import java.time.Instant;

public class SubmitLinkedInPublicProfileCommand implements Command {

    String resumeId;
    Instant createdOn;

    private SubmitLinkedInPublicProfileCommand(Builder builder) {
        resumeId = builder.resumeId;
        createdOn = builder.createdOn;
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public String getResumeId() {
        return resumeId;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public static final class Builder {
        private String resumeId;
        private Instant createdOn;

        private Builder() {
        }

        public Builder resumeId(String resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public Builder createdOn(Instant createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public SubmitLinkedInPublicProfileCommand build() {
            return new SubmitLinkedInPublicProfileCommand(this);
        }
    }
}
