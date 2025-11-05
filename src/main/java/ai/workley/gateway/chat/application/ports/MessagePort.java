package ai.workley.gateway.chat.application.ports;

import ai.workley.gateway.chat.domain.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessagePort {

    Mono<Message<String>> save(Message<String> message);

    Flux<Message<String>> findAll(String chatId);

    Flux<Message<String>> findRecentConversation(String chatId, int limit);
}
