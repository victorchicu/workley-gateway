package ai.workley.gateway.features.chat.app.query.handler;

import ai.workley.gateway.features.chat.domain.query.GetChatInput;
import ai.workley.gateway.features.chat.domain.query.GetChatOutput;
import ai.workley.gateway.features.chat.app.error.ApplicationError;
import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.ChatRepository;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.MessageRepository;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.ChatDocument;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.MessageDocument;
import ai.workley.gateway.features.shared.app.query.handler.QueryHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

@Component
public class GetChatHandler implements QueryHandler<GetChatInput, GetChatOutput> {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public GetChatHandler(ChatRepository chatRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public Class<GetChatInput> supported() {
        return GetChatInput.class;
    }

    @Override
    public Mono<GetChatOutput> handle(Principal actor, GetChatInput query) {
        Set<String> participants = Set.of(actor.getName());
        return chatRepository.findChat(query.chatId(), participants)
                .switchIfEmpty(Mono.error(new ApplicationError("Oops. Chat not found.")))
                .flatMap((ChatDocument chatDocument) ->
                        messageRepository.findAllByChatId(chatDocument.getChatId())
                                .map(GetChatHandler::toMessage)
                                .collectList()
                                .map(messages -> new GetChatOutput(chatDocument.getChatId(), messages))
                );
    }

    private static Message<String> toMessage(MessageDocument<String> messageDocument) {
        return Message.response(
                messageDocument.getId(),
                messageDocument.getChatId(),
                messageDocument.getOwnedBy(),
                messageDocument.getRole(),
                messageDocument.getCreatedAt(),
                messageDocument.getContent()
        );
    }
}
