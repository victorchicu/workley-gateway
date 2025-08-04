package io.zumely.gateway.resume.application.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.zumely.gateway.resume.application.event.impl.ErrorEvent;
import io.zumely.gateway.resume.application.event.impl.CreateResumeEvent;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ErrorEvent.class, name = "ErrorEvent"),
        @JsonSubTypes.Type(value = CreateResumeEvent.class, name = "CreateResumeEvent")
})
public abstract class Event {
    @JsonIgnore
    private final String aggregateId;

    protected Event(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getAggregateId() {
        return aggregateId;
    }
}
