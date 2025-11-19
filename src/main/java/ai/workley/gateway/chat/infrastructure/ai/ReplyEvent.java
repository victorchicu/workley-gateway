package ai.workley.gateway.chat.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChunkReply.class, name = "CHUNK"),
        @JsonSubTypes.Type(value = ErrorReply.class, name = "ERROR")
})
public interface ReplyEvent {

    String type();
}