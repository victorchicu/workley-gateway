package io.zumely.gateway.resume.application.query;

import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.infrastructure.ChatSessionRepository;
import io.zumely.gateway.resume.infrastructure.MessageHistoryRepository;
import io.zumely.gateway.resume.infrastructure.data.MessageObject;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

@Component
public class GetChatQueryHandler implements QueryHandler<GetChatQuery, GetChatQueryResult> {
    private final ChatSessionRepository chatSessionRepository;
    private final MessageHistoryRepository messageHistoryRepository;

    public GetChatQueryHandler(ChatSessionRepository chatSessionRepository, MessageHistoryRepository messageHistoryRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.messageHistoryRepository = messageHistoryRepository;
    }

    @Override
    public Class<GetChatQuery> supported() {
        return GetChatQuery.class;
    }

    @Override
    public Mono<GetChatQueryResult> handle(Principal actor, GetChatQuery query) {
        Set<String> participants = Set.of(actor.getName());
        return chatSessionRepository.findChat(query.chatId(), participants)
                .switchIfEmpty(Mono.error(new ApplicationException("Oops. Chat not found.")))
                .flatMap(chatObject ->
                        messageHistoryRepository.findAllByChatId(chatObject.getId())
                                .map(GetChatQueryHandler::toMessage)
                                .collectList()
                                .map(messages -> new GetChatQueryResult(chatObject.getId(), messages))
                );
    }

    private static Message<String> toMessage(MessageObject<String> messageObject) {
        return Message.create(
                messageObject.getId(), messageObject.getChatId(), messageObject.getAuthor(), messageObject.getRole(), messageObject.getContent()
        );
    }
}