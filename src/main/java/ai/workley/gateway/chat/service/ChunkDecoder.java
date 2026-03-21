package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.ReplyChunk;
import ai.workley.gateway.chat.model.ReplyEvent;

public interface ChunkDecoder {

    ReplyChunk decode(ReplyEvent event);
}