package ai.workley.gateway.features.shared.domain.command;

import ai.workley.gateway.features.chat.domain.command.AddMessageInput;
import ai.workley.gateway.features.chat.domain.command.CreateChatInput;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateChatInput.class, name = "CreateChat"),
        @JsonSubTypes.Type(value = AddMessageInput.class, name = "AddMessage")
})
public interface Command {
}
