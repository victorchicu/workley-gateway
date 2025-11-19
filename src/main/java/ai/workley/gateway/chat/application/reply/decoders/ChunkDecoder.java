package ai.workley.gateway.chat.application.reply.decoders;

import ai.workley.gateway.chat.domain.content.TextContent;
import ai.workley.gateway.chat.infrastructure.ai.ReplyEvent;

public interface ChunkDecoder {

    TextContent decode(ReplyEvent event);
}