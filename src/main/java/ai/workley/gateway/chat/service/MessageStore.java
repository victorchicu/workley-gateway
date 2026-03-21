package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.Message;
import ai.workley.gateway.chat.model.Content;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageStore {

    Mono<Message<? extends Content>> save(Message<? extends Content> message);

    Flux<Message<? extends Content>> loadAll(String chatId);

    Flux<Message<? extends Content>> loadRecent(String chatId, int limit);
}