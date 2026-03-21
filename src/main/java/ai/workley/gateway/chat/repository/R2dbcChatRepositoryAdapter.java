package ai.workley.gateway.chat.repository;

import ai.workley.gateway.chat.service.ChatStore;
import ai.workley.gateway.chat.model.Chat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class R2dbcChatRepositoryAdapter implements ChatStore {
    private final R2dbcChatRepository chatRepository;
    private final R2dbcChatParticipantRepository participantRepository;
    private final ObjectMapper objectMapper;

    public R2dbcChatRepositoryAdapter(
            R2dbcChatRepository chatRepository,
            R2dbcChatParticipantRepository participantRepository,
            ObjectMapper objectMapper
    ) {
        this.chatRepository = chatRepository;
        this.participantRepository = participantRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Chat> saveChat(Chat chat) {
        ChatEntity entity = toChatEntity(chat);
        return chatRepository.save(entity)
                .flatMap(saved -> {
                    var participants = chat.participants().stream()
                            .map(p -> new ChatParticipantEntity()
                                    .setChatId(chat.id())
                                    .setParticipantId(p.getId()))
                            .toList();
                    return participantRepository.saveAll(participants)
                            .collectList()
                            .thenReturn(saved);
                })
                .flatMap(this::toChat);
    }

    @Override
    public Mono<Chat> findChat(String chatId, Collection<String> participants) {
        String[] participantIds = participants.toArray(new String[0]);
        return chatRepository.findChat(chatId, participantIds, participantIds.length)
                .flatMap(this::toChat);
    }

    private Mono<Chat> toChat(ChatEntity source) {
        return participantRepository.findAllByChatId(source.getChatId())
                .map(p -> Chat.Participant.create(p.getParticipantId()))
                .collect(Collectors.toSet())
                .map(participants -> {
                    Chat.Summary summary = null;
                    if (source.getSummary() != null) {
                        try {
                            var summaryMap = objectMapper.readValue(source.getSummary().asString(), SummaryDto.class);
                            summary = Chat.Summary.create(summaryMap.title());
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to deserialize summary", e);
                        }
                    }
                    return Chat.create(source.getChatId(), summary, participants);
                });
    }

    private ChatEntity toChatEntity(Chat source) {
        Json summaryJson = null;
        if (source.summary() != null) {
            try {
                summaryJson = Json.of(objectMapper.writeValueAsString(new SummaryDto(source.summary().getTitle())));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize summary", e);
            }
        }
        return new ChatEntity()
                .setChatId(source.id())
                .setSummary(summaryJson);
    }

    private record SummaryDto(String title) {
    }
}
