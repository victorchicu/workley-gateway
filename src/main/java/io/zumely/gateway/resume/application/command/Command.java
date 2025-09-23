package io.zumely.gateway.resume.application.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.zumely.gateway.resume.application.command.impl.AddMessageCommand;
import io.zumely.gateway.resume.application.command.impl.CreateChatCommand;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateChatCommand.class, name = "CreateChatCommand"),
        @JsonSubTypes.Type(value = AddMessageCommand.class, name = "AddMessageCommand")
})
public interface Command {
}
