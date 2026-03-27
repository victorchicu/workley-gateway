package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Payload;
import ai.workley.core.chat.model.Query;
import ai.workley.core.chat.model.ApplicationError;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AppQueryBus implements QueryBus {
    private final Map<Class<? extends Query>, QueryHandler<? extends Query, ? extends Payload>> handlers;

    public AppQueryBus(List<QueryHandler<? extends Query, ? extends Payload>> source) {
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
