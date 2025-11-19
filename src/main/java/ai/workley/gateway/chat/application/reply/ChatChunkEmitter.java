package ai.workley.gateway.chat.application.reply;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.domain.events.ReplyStarted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class ChatChunkEmitter {
    private static final Logger log = LoggerFactory.getLogger(ChatChunkEmitter.class);

    private final Sinks.Many<Message<? extends Content>> chatSessionSink;

    public ChatChunkEmitter(Sinks.Many<Message<? extends Content>> chatSessionSink) {
        this.chatSessionSink = chatSessionSink;
    }

    public void emit(ReplyStarted e, Message<?> message) {
        Sinks.EmitResult result = chatSessionSink.tryEmitNext(message);
        if (result.isFailure()) {
            log.warn("Dropped value (actor={}, chatId={}, reason={})", e.actor(), e.chatId(), result);
        }
    }
}
