package ai.workley.core.chat.controller;


import ai.workley.core.chat.model.Message;
import ai.workley.core.chat.model.Content;
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
