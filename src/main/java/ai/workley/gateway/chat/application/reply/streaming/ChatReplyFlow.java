package ai.workley.gateway.chat.application.reply.streaming;

import ai.workley.gateway.chat.application.chat.ChatSession;
import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import ai.workley.gateway.chat.application.reply.core.ReplyAggregator;
import ai.workley.gateway.chat.application.reply.processing.ChunkDecoder;
import ai.workley.gateway.chat.application.reply.model.ReplyException;
import ai.workley.gateway.chat.application.reply.core.ReplyFlow;
import ai.workley.gateway.chat.application.reply.processing.PromptBuilder;
import ai.workley.gateway.chat.application.reply.core.ReplyPublisher;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.domain.content.ReplyChunk;
import ai.workley.gateway.chat.domain.content.ReplyError;
import ai.workley.gateway.chat.domain.content.ReplyCompleted;
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
public class ChatReplyFlow implements ReplyFlow {
    private static final Logger log = LoggerFactory.getLogger(ChatReplyFlow.class);

    private final AiModel aiModel;
    private final ChatSession chatSession;
    private final ChunkDecoder chunkDecoder;
    private final PromptBuilder promptBuilder;
    private final ReplyPublisher replyPublisher;
    private final ReplyAggregator replyAggregator;
    private final ChatChunkEmitter chatChunkEmitter;

    public ChatReplyFlow(
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

        Flux<ReplyChunk> chunks = aiModel.stream(prompt)
                .map(chunkDecoder::decode)
                .doOnNext(chunk ->
                        chatChunkEmitter.emit(
                                Message.create(replyId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(),
                                        chunk))
                )
                .doOnComplete(() -> {
                    chatChunkEmitter.emit(
                            Message.create(replyId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(),
                                    new ReplyCompleted("\n")));
                })
                .onErrorResume(ReplyException.class, exception -> {
                    log.error("Error reply: {}", exception.getMessage());
                    chatChunkEmitter.emit(
                            Message.create(replyId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(),
                                    new ReplyError(exception.getCode(), exception.getMessage())));
                    return Flux.empty();
                });

        return replyAggregator.aggregate(chunks)
                .flatMap(fullReply -> {
                    return replyPublisher.publish(
                            Message.create(replyId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(),
                                    new ReplyChunk(fullReply)));
                })
                .then();
    }
}
