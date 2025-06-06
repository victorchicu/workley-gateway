package app.awaytogo.gateway.resume.domain.command;

import java.security.Principal;
import java.time.Instant;

public class CreateResumeCommand implements Command {
    String source;
    String resumeId;
    Instant timestamp;
    Principal principal;

    private CreateResumeCommand(Builder builder) {
        resumeId = builder.resumeId;
        source = builder.source;
        timestamp = builder.timestamp;
        principal = builder.principal;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getSource() {
        return source;
    }

    @Override
    public String getResumeId() {
        return resumeId;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public static final class Builder {
        private String resumeId;
        private String source;
        private Instant timestamp;
        private Principal principal;

        private Builder() {
        }

        public Builder resumeId(String resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder principal(Principal principal) {
            this.principal = principal;
            return this;
        }

        public CreateResumeCommand build() {
            return new CreateResumeCommand(this);
        }
    }
}
