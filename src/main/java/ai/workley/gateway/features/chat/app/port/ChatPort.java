package ai.workley.gateway.features.chat.app.port;

import ai.workley.gateway.features.chat.domain.Chat;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ChatPort {

    Mono<Chat> save(Chat chat);

    Mono<Chat> findChat(String id, Collection<String> participants);
}
