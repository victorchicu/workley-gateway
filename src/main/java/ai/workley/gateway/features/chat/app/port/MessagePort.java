package ai.workley.gateway.features.chat.app.port;

import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.MessageDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessagePort {

    Mono<MessageDocument<String>> save(MessageDocument<String> message);

    Flux<MessageDocument<String>> findAll(String chatId);

}
