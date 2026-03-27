package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Chat;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ChatStore {

    Mono<Chat> saveChat(Chat chat);

    Mono<Chat> findChat(String id, Collection<String> participants);
}
