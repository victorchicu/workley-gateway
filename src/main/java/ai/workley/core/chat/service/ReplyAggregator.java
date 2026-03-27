package ai.workley.core.chat.service;

import ai.workley.core.chat.model.ReplyChunk;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReplyAggregator {

    Mono<String> aggregate(Flux<ReplyChunk> chunks);
}
