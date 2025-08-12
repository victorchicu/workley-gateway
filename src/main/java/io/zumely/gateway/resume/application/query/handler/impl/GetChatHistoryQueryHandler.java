package io.zumely.gateway.resume.application.query.handler.impl;

import io.zumely.gateway.resume.application.event.data.CreateChatApplicationEvent;
import io.zumely.gateway.resume.application.event.data.ApplicationEvent;
import io.zumely.gateway.resume.application.query.data.GetChatHistoryQuery;
import io.zumely.gateway.resume.application.query.data.GetChatHistoryQueryResult;
import io.zumely.gateway.resume.application.query.data.Message;
import io.zumely.gateway.resume.application.query.handler.QueryHandler;
import io.zumely.gateway.resume.infrastructure.eventstore.ChatStore;
import io.zumely.gateway.resume.infrastructure.eventstore.data.StoreEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

@Component
public class GetChatHistoryQueryHandler implements QueryHandler<GetChatHistoryQuery, GetChatHistoryQueryResult> {
    private final ChatStore eventStore;

    public GetChatHistoryQueryHandler(ChatStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public Class<GetChatHistoryQuery> supported() {
        return GetChatHistoryQuery.class;
    }

    @Override
    public Mono<GetChatHistoryQueryResult> handle(Principal actor, GetChatHistoryQuery query) {
        return eventStore.findHistory(actor.getName(), query.chatId()).collectList()
                .map((List<StoreEvent<ApplicationEvent>> events) ->
                        toGetChatHistoryQueryResult(query.chatId(), events));
    }

    private GetChatHistoryQueryResult toGetChatHistoryQueryResult(String chatId, List<StoreEvent<ApplicationEvent>> source) {
        return new GetChatHistoryQueryResult(chatId,
                source.stream()
                        .map((StoreEvent<ApplicationEvent> event) -> {
                            if (event.getEventData() instanceof CreateChatApplicationEvent createChatData) {
                                return new Message<>(
                                        event.getId(),
                                        createChatData.message().text(),
                                        "user",
                                        event.getCreatedAt(),
                                        "sent"
                                );
                            }
                            throw new UnsupportedOperationException(
                                    "Not supported application event "
                                            + event.getEventData().getClass().getSimpleName());
                        })
                        .toList()
        );
    }
}
