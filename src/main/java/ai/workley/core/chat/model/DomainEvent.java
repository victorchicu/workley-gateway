package ai.workley.core.chat.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChatCreated.class, name = "ChatCreated"),
        @JsonSubTypes.Type(value = MessageAdded.class, name = "MessageAdded"),
        @JsonSubTypes.Type(value = EmbeddingSaved.class, name = "EmbeddingSaved"),
        @JsonSubTypes.Type(value = ReplySaved.class, name = "ReplySaved"),
        @JsonSubTypes.Type(value = ReplyCompleted.class, name = "ReplyCompleted"),
        @JsonSubTypes.Type(value = ReplyFailed.class, name = "ReplyFailed"),
        @JsonSubTypes.Type(value = ReplyStarted.class, name = "ReplyStarted")
})
public interface DomainEvent {

}
