package app.awaytogo.gateway.resume.application;

import app.awaytogo.gateway.resume.domain.model.ReadModel;
import app.awaytogo.gateway.resume.domain.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Map;

@Component
public class QueryDispatcher {
    private static final Logger log = LoggerFactory.getLogger(QueryDispatcher.class);

    private final Map<String, QueryHandler<? extends Query, ? extends ReadModel>> queryHandlers;

    public QueryDispatcher(Map<String, QueryHandler<? extends Query, ? extends ReadModel>> queryHandlers) {
        this.queryHandlers = queryHandlers;
    }

    @SuppressWarnings("unchecked")
    public <R extends ReadModel> Mono<R> dispatch(Principal principal, Query query) {
        String queryType = query.getClass().getSimpleName();

        QueryHandler<Query, R> queryHandler = (QueryHandler<Query, R>) queryHandlers.get(queryType);

        if (queryHandler == null) {
            return Mono.error(new IllegalArgumentException(
                    "No handler found for query type: " + queryType
            ));
        }

        log.debug("Dispatching query {} to handler {}",
                queryType, queryHandler.getClass().getSimpleName());

        return queryHandler.handle(principal, query)
                .doOnSuccess(result ->
                        log.debug("Query {} handled successfully",
                                query.resumeId())
                )
                .doOnError(error ->
                        log.error("Error handling query {}: {}",
                                query.resumeId(), error.getMessage()));
    }
}