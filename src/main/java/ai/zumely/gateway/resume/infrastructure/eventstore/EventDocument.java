package ai.zumely.gateway.resume.infrastructure.eventstore;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "events")
public class EventDocument extends BaseDocument {
    private String type;
    private String data;
    private String resumeId;

    private EventDocument(Builder builder) {
        setId(builder.id);
        setVersion(builder.version);
        type = builder.type;
        data = builder.data;
        resumeId = builder.resumeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private String id;
        private Long version;
        private String type;
        private String data;
        private String resumeId;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder data(String data) {
            this.data = data;
            return this;
        }

        public Builder resumeId(String resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public EventDocument build() {
            return new EventDocument(this);
        }
    }
}
