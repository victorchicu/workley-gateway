package io.zumely.gateway.resume.application.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.zumely.gateway.resume.application.event.impl.ErrorEvent;
import io.zumely.gateway.resume.application.event.impl.CreateChatEvent;

import java.security.Principal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ErrorEvent.class, name = "ErrorEvent"),
        @JsonSubTypes.Type(value = CreateChatEvent.class, name = "CreateResumeEvent")
})
public abstract class Event {
    @JsonIgnore
    private final Principal principal;
    @JsonIgnore
    private final String chatId;

    protected Event(Principal principal, String chatId) {
        this.principal = principal;
        this.chatId = chatId;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public String getChatId() {
        return chatId;
    }
}