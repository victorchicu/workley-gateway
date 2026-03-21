package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.service.ChatSession;
import ai.workley.gateway.chat.service.AiModel;
import ai.workley.gateway.chat.service.ReplyAggregator;
import ai.workley.gateway.chat.service.ChunkDecoder;
import ai.workley.gateway.chat.model.ReplyException;
import ai.workley.gateway.chat.service.ReplyFlow;
import ai.workley.gateway.chat.service.PromptBuilder;
import ai.workley.gateway.chat.service.ReplyPublisher;
import ai.workley.gateway.chat.model.Message;
import ai.workley.gateway.chat.model.Role;
import ai.workley.gateway.chat.model.Content;
import ai.workley.gateway.chat.model.ReplyChunk;
import ai.workley.gateway.chat.model.ReplyError;
import ai.workley.gateway.chat.model.ReplyCompletedContent;
import ai.workley.gateway.chat.model.ReplyStarted;
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
                                    new ReplyCompletedContent("\n")));
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
