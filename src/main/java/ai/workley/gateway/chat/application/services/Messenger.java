package ai.workley.gateway.chat.application.services;

import ai.workley.gateway.chat.application.ports.outbound.messenger.ChatStore;
import ai.workley.gateway.chat.application.ports.outbound.messenger.MessageStore;
import ai.workley.gateway.chat.domain.Chat;
import ai.workley.gateway.chat.domain.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Service
public class Messenger {
    private final ChatStore chatStore;
    private final MessageStore messageStore;

    public Messenger(ChatStore chatStore, MessageStore messageStore) {
        this.chatStore = chatStore;
        this.messageStore = messageStore;
    }

    public Mono<Chat> saveChat(Chat chat) {
        return chatStore.save(chat);
    }

    public Mono<Chat> findChat(String id, Collection<String> participants) {
        return chatStore.findChat(id, participants);
    }

    public Mono<Message<String>> addMessage(Message<String> message) {
        return messageStore.save(message);
    }

    public Flux<Message<String>> loadAllHistory(String chatId) {
        return messageStore.loadAll(chatId);
    }

    public Flux<Message<String>> loadRecentHistory(String chatId, int limit) {
        return messageStore.loadRecent(chatId, limit);
    }
}
