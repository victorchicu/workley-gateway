package ai.workley.gateway.chat.application.reply.processing;

import ai.workley.gateway.chat.application.reply.model.ReplyException;
import ai.workley.gateway.chat.application.reply.model.ReplyType;
import ai.workley.gateway.chat.domain.content.TextContent;
import ai.workley.gateway.chat.infrastructure.ai.ChunkReply;
import ai.workley.gateway.chat.infrastructure.ai.ErrorCode;
import ai.workley.gateway.chat.infrastructure.ai.ErrorReply;
import ai.workley.gateway.chat.infrastructure.ai.ReplyEvent;
import org.springframework.stereotype.Component;

@Component
public class ChunkDecoderImpl implements ChunkDecoder {
    @Override
    public TextContent decode(ReplyEvent event) {
        ReplyType replyType = ReplyType.valueOf(event.type());
        return switch (event) {
            case ChunkReply(String text)
                    when replyType == ReplyType.CHUNK -> new TextContent(text);
            case ErrorReply(ErrorCode code, String message)
                    when replyType == ReplyType.ERROR -> throw new ReplyException(code, message);
            default -> throw new UnsupportedOperationException("Unsupported event type: " + event.type());
        };
    }
}
