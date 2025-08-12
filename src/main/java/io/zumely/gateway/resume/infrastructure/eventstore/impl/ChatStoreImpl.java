package io.zumely.gateway.resume.infrastructure.eventstore.impl;

import io.zumely.gateway.resume.application.event.data.ApplicationEvent;
import io.zumely.gateway.resume.infrastructure.eventstore.ChatRepository;
import io.zumely.gateway.resume.infrastructure.eventstore.ChatStore;
import io.zumely.gateway.resume.infrastructure.eventstore.data.StoreObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatStoreImpl implements ChatStore {

    private final ChatRepository chatRepository;

    public ChatStoreImpl(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public Mono<Boolean> exists(String actor, String chatId) {
        return chatRepository.existsBy(actor, chatId);
    }

    @Override
    public <T extends ApplicationEvent> Mono<StoreObject<T>> save(String actor, T object) {
        StoreObject<T> storeObject =
                new StoreObject<T>()
                        .setEventData(object);

        return chatRepository.save(storeObject);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ApplicationEvent> Flux<StoreObject<T>> findHistory(String actor, String chatId) {
        return chatRepository.findAllBy(actor, chatId)
                .map((StoreObject<?> event) -> (StoreObject<T>) event);
    }
}
