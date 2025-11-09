package ai.workley.gateway.chat.application.ports.outbound.messenger;

import ai.workley.gateway.chat.domain.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageStore {

    Mono<Message<String>> save(Message<String> message);

    Flux<Message<String>> loadAll(String chatId);

    Flux<Message<String>> loadRecent(String chatId, int limit);
}
