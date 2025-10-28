package ai.workley.gateway.features.chat.app.query.bus;

import ai.workley.gateway.features.shared.app.command.results.Output;
import ai.workley.gateway.features.shared.domain.query.Query;
import ai.workley.gateway.features.chat.app.error.ApplicationError;
import ai.workley.gateway.features.shared.app.query.handler.QueryHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class QueryBus {
    private final Map<Class<? extends Query>, QueryHandler<? extends Query, ? extends Output>> handlers;

    public QueryBus(List<QueryHandler<? extends Query, ? extends Output>> source) {
        this.handlers = source.stream()
                .collect(Collectors.toMap(QueryHandler::supported,
                        Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Query, R extends Output> Mono<R> execute(Principal actor, T query) {
        QueryHandler<T, R> queryHandler = (QueryHandler<T, R>) handlers.get(query.getClass());

        if (queryHandler == null) {
            throw new ApplicationError(
                    "No handler found for query type " + query.getClass().getSimpleName()
            );
        }

        return queryHandler.handle(actor, query);
    }
}
