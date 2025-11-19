package ai.workley.gateway.chat.application.reply.flows;

import ai.workley.gateway.chat.application.chat.ChatSession;
import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import ai.workley.gateway.chat.application.reply.aggregators.ReplyAggregator;
import ai.workley.gateway.chat.application.reply.decoders.ChunkDecoder;
import ai.workley.gateway.chat.application.reply.exceptions.ReplyException;
import ai.workley.gateway.chat.application.reply.prompts.PromptBuilder;
import ai.workley.gateway.chat.application.reply.publishers.ReplyPublisher;
import ai.workley.gateway.chat.application.reply.emitters.ChatChunkEmitter;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.domain.content.TextContent;
import ai.workley.gateway.chat.domain.events.ReplyStarted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class SimpleTalkReplyFlow implements ReplyFlow {
    private static final Logger log = LoggerFactory.getLogger(SimpleTalkReplyFlow.class);

    private final AiModel aiModel;
    private final ChatSession chatSession;
    private final ChunkDecoder chunkDecoder;
    private final PromptBuilder promptBuilder;
    private final ReplyPublisher replyPublisher;
    private final ReplyAggregator replyAggregator;
    private final ChatChunkEmitter chatChunkEmitter;

    public SimpleTalkReplyFlow(
            AiModel aiModel,
            ChatSession chatSession,
            ChunkDecoder chunkDecoder,
            PromptBuilder promptBuilder,
            ReplyPublisher replyPublisher,
            ReplyAggregator replyAggregator,
            ChatChunkEmitter chatChunkEmitter
    ) {
        this.aiModel = aiModel;
        this.chatSession = chatSession;
        this.chunkDecoder = chunkDecoder;
        this.promptBuilder = promptBuilder;
        this.replyPublisher = replyPublisher;
        this.replyAggregator = replyAggregator;
        this.chatChunkEmitter = chatChunkEmitter;
    }

    @Override
    public Mono<Void> process(ReplyStarted e) {
        return chatSession.loadRecentHistory(e.chatId(), 100)
                .collectList()
                .flatMapMany(history -> streamReply(e, history))
                .then();
    }

    private Mono<Void> streamReply(ReplyStarted e, List<Message<? extends Content>> history) {
        final String replyId = UUID.randomUUID().toString();

        Prompt prompt = promptBuilder.build(e.message(), history);

        Flux<TextContent> chunks = aiModel.stream(prompt)
                .map(chunkDecoder::decode)
                .doOnNext(chunk ->
                        chatChunkEmitter.emit(
                                Message.create(replyId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), chunk))
                )
                .onErrorResume(ReplyException.class, exception -> {
                    log.error("Error streaming reply: {}", exception.getMessage());
                    return Flux.empty();
                });

        return replyAggregator.aggregate(chunks)
                .flatMap(fullReply ->
                        replyPublisher.publish(
                                Message.create(replyId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), new TextContent(fullReply))))
                .then();
    }
}
