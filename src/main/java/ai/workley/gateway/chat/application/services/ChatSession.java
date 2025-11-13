package ai.workley.gateway.chat.application.services;

import ai.workley.gateway.chat.application.ports.outbound.messenger.ChatStore;
import ai.workley.gateway.chat.application.ports.outbound.messenger.MessageStore;
import ai.workley.gateway.chat.domain.Chat;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Service
public class ChatSession {
    private final ChatStore chatStore;
    private final MessageStore messageStore;

    public ChatSession(ChatStore chatStore, MessageStore messageStore) {
        this.chatStore = chatStore;
        this.messageStore = messageStore;
    }

    public Mono<Chat> saveChat(Chat chat) {
        return chatStore.save(chat);
    }

    public Mono<Chat> findChat(String id, Collection<String> participants) {
        return chatStore.findChat(id, participants);
    }

    public Mono<Message<? extends Content>> addMessage(Message<? extends Content> message) {
        return messageStore.save(message);
    }

    public Flux<Message<? extends Content>> loadAllHistory(String chatId) {
        return messageStore.loadAll(chatId);
    }

    public Flux<Message<? extends Content>> loadRecentHistory(String chatId, int limit) {
        return messageStore.loadRecent(chatId, limit);
    }
}
