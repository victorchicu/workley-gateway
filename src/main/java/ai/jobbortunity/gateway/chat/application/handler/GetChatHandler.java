package ai.jobbortunity.gateway.chat.application.handler;

import ai.jobbortunity.gateway.chat.application.query.QueryHandler;
import ai.jobbortunity.gateway.chat.application.result.GetChatResult;
import ai.jobbortunity.gateway.chat.application.error.ApplicationError;
import ai.jobbortunity.gateway.chat.application.query.GetChat;
import ai.jobbortunity.gateway.chat.domain.model.Message;
import ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.entity.ChatModel;
import ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.entity.MessageModel;
import ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.repository.ChatReadRepository;
import ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.repository.MessageReadRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

@Component
public class GetChatHandler implements QueryHandler<GetChat, GetChatResult> {
    private final ChatReadRepository chatReadRepository;
    private final MessageReadRepository messageReadRepository;

    public GetChatHandler(ChatReadRepository chatReadRepository, MessageReadRepository messageReadRepository) {
        this.chatReadRepository = chatReadRepository;
        this.messageReadRepository = messageReadRepository;
    }

    @Override
    public Class<GetChat> supported() {
        return GetChat.class;
    }

    @Override
    public Mono<GetChatResult> handle(Principal actor, GetChat query) {
        Set<String> participants = Set.of(actor.getName());
        return chatReadRepository.findChat(query.chatId(), participants)
                .switchIfEmpty(Mono.error(new ApplicationError("Oops. Chat not found.")))
                .flatMap((ChatModel chatModel) ->
                        messageReadRepository.findAllByChatId(chatModel.getChatId())
                                .map(GetChatHandler::toMessage)
                                .collectList()
                                .map(messages -> new GetChatResult(chatModel.getChatId(), messages))
                );
    }

    private static Message<String> toMessage(MessageModel<String> messageModel) {
        return Message.response(
                messageModel.getId(),
                messageModel.getChatId(),
                messageModel.getOwnedBy(),
                messageModel.getRole(),
                messageModel.getCreatedAt(),
                messageModel.getContent()
        );
    }
}
