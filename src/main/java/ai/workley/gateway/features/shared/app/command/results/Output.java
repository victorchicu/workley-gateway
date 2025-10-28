package ai.workley.gateway.features.shared.app.command.results;

import ai.workley.gateway.features.chat.domain.error.ApplicationErrorOutput;
import ai.workley.gateway.features.chat.domain.command.AddMessageOutput;
import ai.workley.gateway.features.chat.domain.command.CreateChatOutput;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateChatOutput.class, name = "CreateChatInput"),
        @JsonSubTypes.Type(value = AddMessageOutput.class, name = "AddMessageInput"),
        @JsonSubTypes.Type(value = ApplicationErrorOutput.class, name = "BadRequest"),
})
public interface Output {

}