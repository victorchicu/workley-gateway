package io.zumely.gateway.resume.application.query;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class GetChatQueryHandler implements QueryHandler<GetChatQuery, GetChatQueryResult> {

    @Override
    public Class<GetChatQuery> supported() {
        return GetChatQuery.class;
    }

    @Override
    public Mono<GetChatQueryResult> handle(Principal actor, GetChatQuery query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
