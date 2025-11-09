package ai.workley.gateway.chat.infrastructure.messenger;

import ai.workley.gateway.chat.domain.Chat;
import ai.workley.gateway.chat.domain.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface MessengerRepository {
    Mono<Chat> save(Chat chat);

    Mono<Chat> find(String chatId, Collection<String> participants);

    Mono<Message<String>> save(Message<String> message);

    Flux<Message<String>> loadAll(String chatId);

    Flux<Message<String>> loadRecent(String chatId, int limit);
}
