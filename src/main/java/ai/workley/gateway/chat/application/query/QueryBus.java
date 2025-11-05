package ai.workley.gateway.chat.application.query;

import ai.workley.gateway.chat.domain.Payload;
import ai.workley.gateway.chat.domain.query.Query;
import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class QueryBus {
    private final Map<Class<? extends Query>, QueryHandler<? extends Query, ? extends Payload>> handlers;

    public QueryBus(List<QueryHandler<? extends Query, ? extends Payload>> source) {
        this.handlers = source.stream()
                .collect(Collectors.toMap(QueryHandler::supported,
                        Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Query, R extends Payload> Mono<R> execute(Principal actor, T query) {
        QueryHandler<T, R> queryHandler = (QueryHandler<T, R>) handlers.get(query.getClass());

        if (queryHandler == null) {
            throw new ApplicationError(
                    "No handler found for query type " + query.getClass().getSimpleName()
            );
        }

        return queryHandler.handle(actor, query);
    }
}
