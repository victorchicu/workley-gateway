package app.awaytogo.gateway.resume.api.dto;

import java.time.Instant;

public class ErrorResponseDto {
    private String errorCode;
    private String message;
    private Instant timestamp;

    private ErrorResponseDto(Builder builder) {
        errorCode = builder.errorCode;
        message = builder.message;
        timestamp = builder.timestamp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private String errorCode;
        private String message;
        private Instant timestamp;

        private Builder() {
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
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

        public ErrorResponseDto build() {
            return new ErrorResponseDto(this);
        }
    }
}
