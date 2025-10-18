package ai.jobbortunity.gateway.chat.presentation.websocket;


import ai.jobbortunity.gateway.chat.domain.model.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Controller
public class ChatStreamRSocketController {
    private final Sinks.Many<Message<String>> chatSink;

    ChatStreamRSocketController(Sinks.Many<Message<String>> chatSink) {
        this.chatSink = chatSink;
    }

    @MessageMapping("chat.stream.{chatId}")
    public Flux<Message<String>> stream(@DestinationVariable String chatId) {
        return chatSink.asFlux().filter((Message<String> message) -> {
            return chatId.equals(message.chatId());
        });
    }
}
