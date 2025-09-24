package ai.jobbortunity.gateway.chat.application.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ai.jobbortunity.gateway.chat.application.command.impl.AddMessageCommandResult;
import ai.jobbortunity.gateway.chat.application.command.impl.CreateChatCommandResult;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateChatCommandResult.class, name = "CreateChatCommandResult"),
        @JsonSubTypes.Type(value = AddMessageCommandResult.class, name = "AddMessageCommandResult"),
        @JsonSubTypes.Type(value = InternalErrorCommandResult.class, name = "ApplicationExceptionCommandResult")
})
public interface CommandResult {

}
