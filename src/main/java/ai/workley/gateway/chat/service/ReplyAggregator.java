package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.ReplyChunk;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReplyAggregator {

    Mono<String> aggregate(Flux<ReplyChunk> chunks);
}
