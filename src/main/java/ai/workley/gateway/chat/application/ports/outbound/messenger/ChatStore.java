package ai.workley.gateway.chat.application.ports.outbound.messenger;

import ai.workley.gateway.chat.domain.Chat;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ChatStore {

    Mono<Chat> save(Chat chat);

    Mono<Chat> findChat(String id, Collection<String> participants);
}
