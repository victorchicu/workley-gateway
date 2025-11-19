package ai.workley.gateway.chat.application.reply.core;

import ai.workley.gateway.chat.domain.content.TextContent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReplyAggregator {

    Mono<String> aggregate(Flux<TextContent> chunks);
}
