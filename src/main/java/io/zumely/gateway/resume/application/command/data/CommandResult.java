package io.zumely.gateway.resume.application.command.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.zumely.gateway.resume.interfaces.rest.CommandController;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateChatCommandResult.class, name = "CreateChatCommandResult"),
        @JsonSubTypes.Type(value = AddMessageCommandResult.class, name = "SendMessageCommandResult"),
        @JsonSubTypes.Type(value = ApplicationExceptionCommandResult.class, name = "ApplicationExceptionCommandResult")
})
public interface CommandResult {

}