package ai.workley.gateway.chat.application.reply.processing;

import ai.workley.gateway.chat.domain.content.ReplyChunk;
import ai.workley.gateway.chat.infrastructure.ai.ReplyEvent;

public interface ChunkDecoder {

    ReplyChunk decode(ReplyEvent event);
}