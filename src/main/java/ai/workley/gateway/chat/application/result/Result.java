package ai.workley.gateway.chat.application.result;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateChatResult.class, name = "CreateChatResult"),
        @JsonSubTypes.Type(value = AddMessageResult.class, name = "AddMessageResult"),
        @JsonSubTypes.Type(value = BadRequestResult.class, name = "BadRequestResult"),
})
public interface Result {

}
