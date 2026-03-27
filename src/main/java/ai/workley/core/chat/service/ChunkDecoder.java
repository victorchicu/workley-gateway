package ai.workley.core.chat.service;

import ai.workley.core.chat.model.ReplyChunk;
import ai.workley.core.chat.model.ReplyEvent;

public interface ChunkDecoder {

    ReplyChunk decode(ReplyEvent event);
}
