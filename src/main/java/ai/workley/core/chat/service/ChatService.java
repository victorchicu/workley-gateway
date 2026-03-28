package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Chat;
import ai.workley.core.chat.model.Content;
import ai.workley.core.chat.model.Message;
import ai.workley.core.chat.model.Role;
import ai.workley.core.chat.model.ReplyChunk;
import ai.workley.core.chat.model.CreateChatPayload;
import ai.workley.core.chat.model.AddMessagePayload;
import ai.workley.core.chat.model.GetChatPayload;
import ai.workley.core.chat.model.ApplicationError;
import ai.workley.core.chat.model.Payload;
import ai.workley.core.idempotency.IdempotencyGuard;
import ai.workley.core.idempotency.IdempotencyKeyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatSession chatSession;
    private final ChatReplyFlow chatReplyFlow;
    private final IdGenerator idGenerator;
    private final TransactionalOperator transactionalOperator;
    private final IdempotencyGuard idempotencyGuard;

    public ChatService(
            ChatSession chatSession,
            ChatReplyFlow chatReplyFlow,
            IdGenerator idGenerator,
            TransactionalOperator transactionalOperator,
            IdempotencyGuard idempotencyGuard
    ) {
        this.chatSession = chatSession;
        this.chatReplyFlow = chatReplyFlow;
        this.idGenerator = idGenerator;
        this.transactionalOperator = transactionalOperator;
        this.idempotencyGuard = idempotencyGuard;
    }

    public Mono<GetChatPayload> getChat(String actor, String chatId) {
        return chatSession.findChat(chatId, Set.of(actor))
                .switchIfEmpty(Mono.error(new ApplicationError("Oops. Chat not found.")))
                .flatMap(chat ->
                        chatSession.loadAllHistory(chat.id())
                                .collectList()
                                .map(messages -> new GetChatPayload(chat.id(), messages))
                );
    }

    public Mono<CreateChatPayload> createChat(String userId, String prompt) {
        return Mono.deferContextual(contextView -> {
            String idempotencyKey = IdempotencyKeyContext.get(contextView);

            Mono<CreateChatPayload> operation = Mono.defer(() -> {
                String chatId = idGenerator.generate();
                String messageId = UUID.randomUUID().toString();

                Chat chat = Chat.create(chatId, Chat.Summary.create(prompt), Set.of(Chat.Participant.create(userId)));
                Message<ReplyChunk> message = Message.create(messageId, chatId, userId, Role.ANONYMOUS, Instant.now(), new ReplyChunk(prompt));

                return transactionalOperator.transactional(chatSession.saveChat(chat).then(chatSession.addMessage(message)).thenReturn(CreateChatPayload.ack(chatId, message))).doOnSuccess(payload -> {
                    chatReplyFlow.generate(userId, chatId, message)
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnError(error -> log.error("Reply generation failed (chatId={})", chatId, error))
                            .subscribe();
                });
            });

            return withIdempotency(idempotencyKey, operation);
        }).onErrorMap(error -> {
            if (error instanceof ApplicationError) return error;
            log.error("Could not create chat", error);
            return new ApplicationError("Oops! Could not create chat.");
        });
    }

    public Mono<AddMessagePayload> addMessage(String userId, String chatId, String text) {
        return Mono.deferContextual(contextView -> {
            String idempotencyKey = IdempotencyKeyContext.get(contextView);

            Mono<AddMessagePayload> operation = Mono.defer(() -> {
                String messageId = UUID.randomUUID().toString();
                Message<ReplyChunk> message = Message.create(messageId, chatId, userId, Role.ANONYMOUS, Instant.now(), new ReplyChunk(text));
                return chatSession.addMessage(message).thenReturn(AddMessagePayload.ack(chatId, message));
            }).doOnSuccess(payload -> {
                chatReplyFlow.generate(userId, chatId, payload.message())
                        .subscribeOn(Schedulers.boundedElastic())
                        .doOnError(error -> log.error("Reply generation failed (chatId={})", chatId, error))
                        .subscribe();
            });

            return withIdempotency(idempotencyKey, operation);
        }).onErrorMap(error -> {
            if (error instanceof ApplicationError) return error;
            log.error("Could not add message (chatId={})", chatId, error);
            return new ApplicationError("Oops! Something went wrong. Please try again.");
        });
    }

    @SuppressWarnings("unchecked")
    private <R extends Payload> Mono<R> withIdempotency(String idempotencyKey, Mono<R> operation) {
        return idempotencyGuard.tryAcquire(idempotencyKey)
                .map(cached -> (R) cached)
                .switchIfEmpty(
                        operation.flatMap(payload ->
                                        idempotencyGuard.markCompleted(idempotencyKey, payload)
                                                .thenReturn(payload))
                                .onErrorResume(error ->
                                        idempotencyGuard.markFailed(idempotencyKey)
                                                .then(Mono.error(error)))
                );
    }
}
