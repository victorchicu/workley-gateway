package ai.workley.gateway.features.chat.app.port;

import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.ChatDocument;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ChatPort {

    Mono<ChatDocument> save(ChatDocument chat);

    Mono<ChatDocument> findChat(String id, Collection<String> participants);
}
