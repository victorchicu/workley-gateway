package ai.workley.gateway.features.chat.app.query.handler;

import ai.workley.gateway.features.chat.app.port.ChatPort;
import ai.workley.gateway.features.chat.app.port.MessagePort;
import ai.workley.gateway.features.chat.domain.Chat;
import ai.workley.gateway.features.chat.domain.query.GetChatInput;
import ai.workley.gateway.features.chat.domain.query.GetChatOutput;
import ai.workley.gateway.features.chat.app.error.ApplicationError;
import ai.workley.gateway.features.shared.app.query.handler.QueryHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

@Component
public class GetChatHandler implements QueryHandler<GetChatInput, GetChatOutput> {
    private final ChatPort chatPort;
    private final MessagePort messagePort;

    public GetChatHandler(ChatPort chatPort, MessagePort messagePort) {
        this.chatPort = chatPort;
        this.messagePort = messagePort;
    }

    @Override
    public Class<GetChatInput> supported() {
        return GetChatInput.class;
    }

    @Override
    public Mono<GetChatOutput> handle(Principal actor, GetChatInput query) {
        Set<String> participants = Set.of(actor.getName());
        return chatPort.findChat(query.chatId(), participants)
                .switchIfEmpty(Mono.error(new ApplicationError("Oops. Chat not found.")))
                .flatMap((Chat chat) ->
                        messagePort.findAll(chat.id()).collectList()
                                .map(messages -> new GetChatOutput(chat.id(), messages))
                );
    }
}
