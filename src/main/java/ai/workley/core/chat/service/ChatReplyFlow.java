package ai.workley.core.chat.service;

import ai.workley.core.chat.model.ReplyException;
import ai.workley.core.chat.model.Message;
import ai.workley.core.chat.model.Role;
import ai.workley.core.chat.model.Content;
import ai.workley.core.chat.model.ReplyChunk;
import ai.workley.core.chat.model.ReplyError;
import ai.workley.core.chat.model.ReplyCompletedContent;
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
public class ChatReplyFlow {
    private static final Logger log = LoggerFactory.getLogger(ChatReplyFlow.class);

    private final AiModel aiModel;
    private final ChatSession chatSession;
    private final ChunkDecoder chunkDecoder;
    private final PromptBuilder promptBuilder;
    private final ReplyAggregator replyAggregator;
    private final ChatChunkEmitter chatChunkEmitter;

    public ChatReplyFlow(
            AiModel aiModel,
            ChatSession chatSession,
            ChunkDecoder chunkDecoder,
            PromptBuilder promptBuilder,
            ReplyAggregator replyAggregator,
            ChatChunkEmitter chatChunkEmitter
    ) {
        this.aiModel = aiModel;
        this.chatSession = chatSession;
        this.chunkDecoder = chunkDecoder;
        this.promptBuilder = promptBuilder;
        this.replyAggregator = replyAggregator;
        this.chatChunkEmitter = chatChunkEmitter;
    }

    public Mono<Void> generate(String actor, String chatId, Message<? extends Content> message) {
        return chatSession.loadRecentHistory(chatId, 100)
                .collectList()
                .flatMap(history -> streamReply(actor, chatId, message, history));
    }

    private Mono<Void> streamReply(String actor, String chatId, Message<? extends Content> message,
                                    List<Message<? extends Content>> history) {
        final String replyId = UUID.randomUUID().toString();

        Prompt prompt = promptBuilder.build(message, history);

        Flux<ReplyChunk> chunks = aiModel.stream(prompt)
                .map(chunkDecoder::decode)
                .doOnNext(chunk ->
                        chatChunkEmitter.emit(
                                Message.create(replyId, chatId, actor, Role.ASSISTANT, Instant.now(), chunk))
                )
                .doOnComplete(() ->
                        chatChunkEmitter.emit(
                                Message.create(replyId, chatId, actor, Role.ASSISTANT, Instant.now(),
                                        new ReplyCompletedContent("\n")))
                )
                .onErrorResume(ReplyException.class, exception -> {
                    log.error("Error reply: {}", exception.getMessage());
                    chatChunkEmitter.emit(
                            Message.create(replyId, chatId, actor, Role.ASSISTANT, Instant.now(),
                                    new ReplyError(exception.getCode(), exception.getMessage())));
                    return Flux.empty();
                });

        return replyAggregator.aggregate(chunks)
                .flatMap(fullReply -> {
                    Message<ReplyChunk> replyMessage = Message.create(
                            replyId, chatId, actor, Role.ASSISTANT, Instant.now(), new ReplyChunk(fullReply));
                    return chatSession.addMessage(replyMessage);
                })
                .then();
    }
}
