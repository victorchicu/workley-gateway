package ai.workley.core.chat.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateChatPayload.class, name = "CreateChat"),
        @JsonSubTypes.Type(value = AddMessagePayload.class, name = "AddMessage")
})
public interface Query {
}
