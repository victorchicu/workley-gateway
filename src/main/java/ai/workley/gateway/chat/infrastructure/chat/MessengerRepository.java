package ai.workley.gateway.chat.infrastructure.chat;

import ai.workley.gateway.chat.domain.Chat;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface MessengerRepository {

    Mono<Chat> save(Chat chat);

    Mono<Chat> find(String chatId, Collection<String> participants);

    Mono<Message<? extends Content>> save(Message<? extends Content> message);

    Flux<Message<? extends Content>> loadAll(String chatId);

    Flux<Message<? extends Content>> loadRecent(String chatId, int limit);
}
