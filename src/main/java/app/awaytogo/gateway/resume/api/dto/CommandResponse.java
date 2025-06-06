package app.awaytogo.gateway.resume.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.Instant;
import java.util.Map;

@JsonDeserialize(builder = CommandResponse.Builder.class)
public class CommandResponse {
    private final String resumeId;
    private final String aggregateId;
    private final String processingState;
    private final String message;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    private CommandResponse(Builder builder) {
        resumeId = builder.resumeId;
        aggregateId = builder.aggregateId;
        processingState = builder.processingState;
        message = builder.message;
        timestamp = builder.timestamp;
        metadata = builder.metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getResumeId() {
        return resumeId;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getProcessingState() {
        return processingState;
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

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String resumeId;
        private String aggregateId;
        private String processingState;
        private String message;
        private Instant timestamp;
        private Map<String, Object> metadata;

        private Builder() {
        }

        public Builder resumeId(String resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder processingState(String processingState) {
            this.processingState = processingState;
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

        public CommandResponse build() {
            return new CommandResponse(this);
        }
    }
}
