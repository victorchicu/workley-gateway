package ai.workley.gateway.chat.application.ports.outbound;

import ai.workley.gateway.chat.domain.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageService {

    Mono<Message<String>> save(Message<String> message);

    Flux<Message<String>> loadAll(String chatId);

    Flux<Message<String>> loadRecent(String chatId, int limit);
}
