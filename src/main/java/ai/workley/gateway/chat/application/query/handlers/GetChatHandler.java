package ai.workley.gateway.chat.application.query.handlers;

import ai.workley.gateway.chat.application.ports.outbound.ChatService;
import ai.workley.gateway.chat.application.ports.outbound.MessageService;
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
    private final ChatService chatService;
    private final MessageService messageService;

    public GetChatHandler(ChatService chatService, MessageService messageService) {
        this.chatService = chatService;
        this.messageService = messageService;
    }

    @Override
    public Class<GetChat> supported() {
        return GetChat.class;
    }

    @Override
    public Mono<GetChatPayload> handle(Principal actor, GetChat query) {
        Set<String> participants = Set.of(actor.getName());
        return chatService.findChat(query.chatId(), participants)
                .switchIfEmpty(Mono.error(new ApplicationError("Oops. Chat not found.")))
                .flatMap((Chat chat) ->
                        messageService.loadAll(chat.id()).collectList()
                                .map(messages -> new GetChatPayload(chat.id(), messages))
                );
    }
}
