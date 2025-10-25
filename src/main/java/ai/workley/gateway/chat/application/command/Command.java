package ai.workley.gateway.chat.application.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateChat.class, name = "CreateChat"),
        @JsonSubTypes.Type(value = AddMessage.class, name = "AddMessage")
})
public interface Command {
}
