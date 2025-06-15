package app.awaytogo.gateway.resume.api.dto;

import app.awaytogo.gateway.resume.domain.aggregate.ResumeAggregate;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.Instant;
import java.util.Map;

@JsonDeserialize(builder = SubmitProfileLinkResponseDto.Builder.class)
public class SubmitProfileLinkResponseDto {
    private final String resumeId;
    private final String message;
    private final Instant timestamp;
    private final Map<String, Object> metadata;
    private final ResumeAggregate.State state;

    private SubmitProfileLinkResponseDto(Builder builder) {
        resumeId = builder.resumeId;
        message = builder.message;
        timestamp = builder.timestamp;
        metadata = builder.metadata;
        state = builder.state;
    }

    public String getResumeId() {
        return resumeId;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public ResumeAggregate.State getState() {
        return state;
    }

    public static Builder builder() {
        return new Builder();
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String resumeId;
        private String message;
        private Instant timestamp;
        private Map<String, Object> metadata;
        private ResumeAggregate.State state;

        private Builder() {
        }

        public Builder resumeId(String resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder state(ResumeAggregate.State state) {
            this.state = state;
            return this;
        }

        public SubmitProfileLinkResponseDto build() {
            return new SubmitProfileLinkResponseDto(this);
        }
    }
}
