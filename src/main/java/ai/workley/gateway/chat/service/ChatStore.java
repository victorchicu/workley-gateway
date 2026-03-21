package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.Chat;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ChatStore {

    Mono<Chat> saveChat(Chat chat);

    Mono<Chat> findChat(String id, Collection<String> participants);
}
