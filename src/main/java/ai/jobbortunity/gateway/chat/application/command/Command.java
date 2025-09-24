package ai.jobbortunity.gateway.chat.application.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ai.jobbortunity.gateway.chat.application.command.impl.AddMessageCommand;
import ai.jobbortunity.gateway.chat.application.command.impl.CreateChatCommand;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateChatCommand.class, name = "CreateChatCommand"),
        @JsonSubTypes.Type(value = AddMessageCommand.class, name = "AddMessageCommand")
})
public interface Command {
}
