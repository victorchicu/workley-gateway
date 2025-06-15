package app.awaytogo.gateway.resume.domain.command.impl;

import app.awaytogo.gateway.resume.domain.command.Command;

import java.time.Instant;

public class SubmitProfileLinkCommand implements Command {

    String resumeId;
    Instant timestamp;

    private SubmitProfileLinkCommand(Builder builder) {
        resumeId = builder.resumeId;
        timestamp = builder.timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public String getResumeId() {
        return resumeId;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }


    public static final class Builder {
        private String resumeId;
        private Instant timestamp;

        private Builder() {
        }

        public Builder resumeId(String resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SubmitProfileLinkCommand build() {
            return new SubmitProfileLinkCommand(this);
        }
    }
}
