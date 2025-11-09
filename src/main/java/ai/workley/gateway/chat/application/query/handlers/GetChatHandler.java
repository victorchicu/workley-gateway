package ai.workley.gateway.chat.application.query.handlers;

import ai.workley.gateway.chat.application.ports.outbound.ChatSession;
import ai.workley.gateway.chat.application.ports.outbound.MessageHistory;
import ai.workley.gateway.chat.domain.Chat;
import ai.workley.gateway.chat.domain.query.GetChat;
import ai.workley.gateway.chat.domain.payloads.GetChatPayload;
import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.application.query.QueryHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

@Component
public class GetChatHandler implements QueryHandler<GetChat, GetChatPayload> {
    private final ChatSession chatSession;
    private final MessageHistory messageHistory;

    public GetChatHandler(ChatSession chatSession, MessageHistory messageHistory) {
        this.chatSession = chatSession;
        this.messageHistory = messageHistory;
    }

    @Override
    public Class<GetChat> supported() {
        return GetChat.class;
    }

    @Override
    public Mono<GetChatPayload> handle(Principal actor, GetChat query) {
        Set<String> participants = Set.of(actor.getName());
        return chatSession.findChat(query.chatId(), participants)
                .switchIfEmpty(Mono.error(new ApplicationError("Oops. Chat not found.")))
                .flatMap((Chat chat) ->
                        messageHistory.loadAll(chat.id()).collectList()
                                .map(messages -> new GetChatPayload(chat.id(), messages))
                );
    }
}
