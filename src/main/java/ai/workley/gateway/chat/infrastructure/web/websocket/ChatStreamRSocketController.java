package ai.workley.gateway.chat.infrastructure.web.websocket;


import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.TextContent;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Controller
public class ChatStreamRSocketController {
    private final Sinks.Many<Message<TextContent>> chatSink;

    ChatStreamRSocketController(Sinks.Many<Message<TextContent>> chatSink) {
        this.chatSink = chatSink;
    }

    @MessageMapping("chat.stream.{chatId}")
    public Flux<Message<TextContent>> stream(@DestinationVariable String chatId) {
        return chatSink.asFlux().filter((Message<TextContent> message) -> {
            return chatId.equals(message.chatId());
        });
    }
}
