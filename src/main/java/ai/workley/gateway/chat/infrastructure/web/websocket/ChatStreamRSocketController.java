package ai.workley.gateway.chat.infrastructure.web.websocket;


import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Controller
public class ChatStreamRSocketController {
    private final Sinks.Many<Message<? extends Content>> chatSessionSink;

    ChatStreamRSocketController(Sinks.Many<Message<? extends Content>> chatSessionSink) {
        this.chatSessionSink = chatSessionSink;
    }

    @MessageMapping("chat.stream.{chatId}")
    public Flux<Message<? extends Content>> stream(@DestinationVariable String chatId) {
        return chatSessionSink.asFlux().filter((Message<? extends Content> message) -> {
            return chatId.equals(message.chatId());
        });
    }
}
