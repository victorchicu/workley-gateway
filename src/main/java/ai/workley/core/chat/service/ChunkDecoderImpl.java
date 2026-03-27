package ai.workley.core.chat.service;

import ai.workley.core.chat.model.ReplyException;
import ai.workley.core.chat.model.ReplyType;
import ai.workley.core.chat.model.ReplyChunk;
import ai.workley.core.chat.model.ChunkReply;
import ai.workley.core.chat.model.ErrorCode;
import ai.workley.core.chat.model.ErrorReply;
import ai.workley.core.chat.model.ReplyEvent;
import org.springframework.stereotype.Component;

@Component
public class ChunkDecoderImpl implements ChunkDecoder {
    @Override
    public ReplyChunk decode(ReplyEvent event) {
        ReplyType replyType = ReplyType.valueOf(event.type());
        return switch (event) {
            case ChunkReply(String text)
                    when replyType == ReplyType.TEXT_CHUNK -> new ReplyChunk(text);
            case ErrorReply(ErrorCode code, String message)
                    when replyType == ReplyType.ERROR_REPLY -> throw new ReplyException(code, message);
            default -> throw new UnsupportedOperationException("Unsupported event type: " + event.type());
        };
    }
}
