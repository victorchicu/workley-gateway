package io.zumely.gateway.resume.application.query.handler.impl;

import io.zumely.gateway.resume.application.query.data.GetChatHistoryQuery;
import io.zumely.gateway.resume.application.query.data.GetChatHistoryQueryResult;
import io.zumely.gateway.resume.application.query.handler.QueryHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class GetChatHistoryQueryHandler implements QueryHandler<GetChatHistoryQuery, GetChatHistoryQueryResult> {

    @Override
    public Class<GetChatHistoryQuery> supported() {
        return GetChatHistoryQuery.class;
    }

    @Override
    public Mono<GetChatHistoryQueryResult> handle(Principal actor, GetChatHistoryQuery query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
