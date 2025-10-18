package ai.jobbortunity.gateway.chat.application.bus;

import ai.jobbortunity.gateway.chat.application.result.QueryResult;
import ai.jobbortunity.gateway.chat.application.error.ApplicationError;
import ai.jobbortunity.gateway.chat.application.query.Query;
import ai.jobbortunity.gateway.chat.application.query.QueryHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class QueryBus {
    private final Map<Class<? extends Query>, QueryHandler<? extends Query, ? extends QueryResult>> handlers;

    public QueryBus(List<QueryHandler<? extends Query, ? extends QueryResult>> source) {
        this.handlers = source.stream()
                .collect(Collectors.toMap(QueryHandler::supported,
                        Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Query, R extends QueryResult> Mono<R> execute(Principal actor, T query) {
        QueryHandler<T, R> queryHandler = (QueryHandler<T, R>) handlers.get(query.getClass());

        if (queryHandler == null) {
            throw new ApplicationError(
                    "No handler found for query type " + query.getClass().getSimpleName()
            );
        }

        return queryHandler.handle(actor, query);
    }
}
