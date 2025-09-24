package ai.jobbortunity.gateway.chat.application.query;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.exception.ApplicationException;
import ai.jobbortunity.gateway.chat.infrastructure.ChatSessionRepository;
import ai.jobbortunity.gateway.chat.infrastructure.MessageHistoryRepository;
import ai.jobbortunity.gateway.chat.infrastructure.data.ChatObject;
import ai.jobbortunity.gateway.chat.infrastructure.data.MessageObject;
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
                .flatMap((ChatObject chatObject) ->
                        messageHistoryRepository.findAllByChatId(chatObject.getChatId())
                                .map(GetChatQueryHandler::toMessage)
                                .collectList()
                                .map(messages -> new GetChatQueryResult(chatObject.getChatId(), messages))
                );
    }

    private static Message<String> toMessage(MessageObject<String> messageObject) {
        return Message.create(
                messageObject.getId(), messageObject.getChatId(), messageObject.getAuthorId(), messageObject.getRole(), messageObject.getContent()
        );
    }
}
