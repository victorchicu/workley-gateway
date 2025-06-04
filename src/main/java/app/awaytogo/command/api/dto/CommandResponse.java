package app.awaytogo.command.api.dto;

import java.time.Instant;
import java.util.Map;

public class CommandResponse {
    private final String commandId;
    private final String aggregateId;
    private final String status;
    private final String message;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    private CommandResponse(Builder builder) {
        commandId = builder.commandId;
        aggregateId = builder.aggregateId;
        status = builder.status;
        message = builder.message;
        timestamp = builder.timestamp;
        metadata = builder.metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCommandId() {
        return commandId;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getStatus() {
        return status;
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

    public static final class Builder {
        private String commandId;
        private String aggregateId;
        private String status;
        private String message;
        private Instant timestamp;
        private Map<String, Object> metadata;

        private Builder() {
        }

        public Builder commandId(String commandId) {
            this.commandId = commandId;
            return this;
        }

        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
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
