package ai.workley.gateway.chat.application.reply.processing;

import ai.workley.gateway.chat.application.reply.core.ReplyAggregator;
import ai.workley.gateway.chat.domain.content.TextContent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TextReplyAggregator implements ReplyAggregator {
    public Mono<String> aggregate(Flux<TextContent> chunks) {
        return chunks
                .reduce(new StringBuilder(), (textBuilder, chunk) -> textBuilder.append(chunk.value()))
                .map(StringBuilder::toString)
                .defaultIfEmpty("");
    }
}