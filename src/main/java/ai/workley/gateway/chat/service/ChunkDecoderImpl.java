package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.ReplyException;
import ai.workley.gateway.chat.model.ReplyType;
import ai.workley.gateway.chat.model.ReplyChunk;
import ai.workley.gateway.chat.model.ChunkReply;
import ai.workley.gateway.chat.model.ErrorCode;
import ai.workley.gateway.chat.model.ErrorReply;
import ai.workley.gateway.chat.model.ReplyEvent;
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