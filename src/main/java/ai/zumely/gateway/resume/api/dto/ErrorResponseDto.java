package ai.zumely.gateway.resume.api.dto;

import java.time.Instant;

public class ErrorResponseDto {
    private String errorCode;
    private String message;
    private Instant createdOn;

    private ErrorResponseDto(Builder builder) {
        errorCode = builder.errorCode;
        message = builder.message;
        createdOn = builder.createdOn;
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

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private String errorCode;
        private String message;
        private Instant createdOn;

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

        public Builder createdOn(Instant createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public ErrorResponseDto build() {
            return new ErrorResponseDto(this);
        }
    }
}
