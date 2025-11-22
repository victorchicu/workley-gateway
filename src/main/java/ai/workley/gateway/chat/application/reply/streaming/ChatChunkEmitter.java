package ai.workley.gateway.chat.application.reply.streaming;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
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

    public void emit(Message<?> message) {
        Sinks.EmitResult result = chatSessionSink.tryEmitNext(message);
        if (result.isFailure()) {
            log.warn("ReplyChunk dropped (actor={}, chatId={}, reason={})", message.ownedBy(), message.chatId(), result);
        }
    }
}