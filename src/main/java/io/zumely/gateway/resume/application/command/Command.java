package io.zumely.gateway.resume.application.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.zumely.gateway.resume.application.command.data.CreateChatCommand;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateChatCommand.class, name = "CreateChatCommand")
})
public interface Command {
}