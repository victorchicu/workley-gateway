package ai.jobbortunity.gateway.chat.application.query;

import ai.jobbortunity.gateway.chat.application.exception.ApplicationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class QueryDispatcher {
    private final Map<Class<? extends Query>, QueryHandler<?, ?>> handlers;

    public QueryDispatcher(List<QueryHandler<?, ?>> source) {
        this.handlers = source.stream()
                .collect(Collectors.toMap(QueryHandler::supported,
                        Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Query, R extends QueryResult> Mono<R> dispatch(Principal actor, T query) {
        QueryHandler<T, R> queryHandler = (QueryHandler<T, R>) handlers.get(query.getClass());

        if (queryHandler == null) {
            throw new ApplicationException(
                    "No handler found for query type " + query.getClass().getSimpleName()
            );
        }

        return queryHandler.handle(actor, query);
    }
}
