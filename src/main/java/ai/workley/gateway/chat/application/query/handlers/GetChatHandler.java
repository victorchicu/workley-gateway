package ai.workley.gateway.chat.application.query.handlers;

import ai.workley.gateway.chat.application.ports.outbound.ChatStore;
import ai.workley.gateway.chat.application.ports.outbound.MessageStore;
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
    private final ChatStore chatStore;
    private final MessageStore messageStore;

    public GetChatHandler(ChatStore chatStore, MessageStore messageStore) {
        this.chatStore = chatStore;
        this.messageStore = messageStore;
    }

    @Override
    public Class<GetChat> supported() {
        return GetChat.class;
    }

    @Override
    public Mono<GetChatPayload> handle(Principal actor, GetChat query) {
        Set<String> participants = Set.of(actor.getName());
        return chatStore.findChat(query.chatId(), participants)
                .switchIfEmpty(Mono.error(new ApplicationError("Oops. Chat not found.")))
                .flatMap((Chat chat) ->
                        messageStore.loadAll(chat.id()).collectList()
                                .map(messages -> new GetChatPayload(chat.id(), messages))
                );
    }
}
