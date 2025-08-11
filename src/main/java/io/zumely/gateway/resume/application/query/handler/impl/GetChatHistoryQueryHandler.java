package io.zumely.gateway.resume.application.query.handler.impl;

import io.zumely.gateway.resume.application.event.ApplicationEvent;
import io.zumely.gateway.resume.application.event.CreateChatApplicationEvent;
import io.zumely.gateway.resume.application.query.data.GetChatHistoryQuery;
import io.zumely.gateway.resume.application.query.data.GetChatHistoryQueryResult;
import io.zumely.gateway.resume.application.query.data.Message;
import io.zumely.gateway.resume.application.query.handler.QueryHandler;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.eventstore.data.StoredEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

@Component
public class GetChatHistoryQueryHandler implements QueryHandler<GetChatHistoryQuery, GetChatHistoryQueryResult> {
    private final EventStore eventStore;

    public GetChatHistoryQueryHandler(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public Class<GetChatHistoryQuery> supported() {
        return GetChatHistoryQuery.class;
    }

    @Override
    public Mono<GetChatHistoryQueryResult> handle(Principal actor, GetChatHistoryQuery query) {
        return eventStore.getChatHistory(actor, query.chatId()).collectList()
                .map((List<StoredEvent<ApplicationEvent>> events) ->
                        toGetChatHistoryQueryResult(actor, query.chatId(), events));
    }

    private GetChatHistoryQueryResult toGetChatHistoryQueryResult(Principal actor, String chatId, List<StoredEvent<ApplicationEvent>> source) {
        return new GetChatHistoryQueryResult(chatId,
                source.stream()
                        .map((StoredEvent<ApplicationEvent> event) -> {
                            if (event.getData() instanceof CreateChatApplicationEvent createChatApplicationEvent) {
                                return new Message<>(
                                        event.getId(),
                                        createChatApplicationEvent.prompt().text(),
                                        "user",
                                        event.getCreatedOn(),
                                        "sent"
                                );
                            }
                            throw new UnsupportedOperationException(
                                    "Not supported application event "
                                            + event.getData().getClass().getSimpleName());
                        })
                        .toList()
        );
    }
}
