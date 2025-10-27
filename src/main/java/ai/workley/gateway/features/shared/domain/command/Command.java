package ai.workley.gateway.features.shared.domain.command;

import ai.workley.gateway.features.chat.domain.command.AddMessage;
import ai.workley.gateway.features.chat.domain.command.CreateChat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateChat.class, name = "CreateChat"),
        @JsonSubTypes.Type(value = AddMessage.class, name = "AddMessage")
})
public interface Command {
}
