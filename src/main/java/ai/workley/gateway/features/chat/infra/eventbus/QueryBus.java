package ai.workley.gateway.features.chat.infra.eventbus;

import ai.workley.gateway.features.shared.domain.query.Query;
import ai.workley.gateway.features.chat.domain.error.ApplicationError;
import ai.workley.gateway.features.shared.app.query.QueryHandler;
import ai.workley.gateway.features.shared.app.command.results.Result;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class QueryBus {
    private final Map<Class<? extends Query>, QueryHandler<? extends Query, ? extends Result>> handlers;

    public QueryBus(List<QueryHandler<? extends Query, ? extends Result>> source) {
        this.handlers = source.stream()
                .collect(Collectors.toMap(QueryHandler::supported,
                        Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Query, R extends Result> Mono<R> execute(Principal actor, T query) {
        QueryHandler<T, R> queryHandler = (QueryHandler<T, R>) handlers.get(query.getClass());

        if (queryHandler == null) {
            throw new ApplicationError(
                    "No handler found for query type " + query.getClass().getSimpleName()
            );
        }

        return queryHandler.handle(actor, query);
    }
}
