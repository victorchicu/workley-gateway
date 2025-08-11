package io.zumely.gateway.resume.application.query;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.command.data.CommandResult;
import io.zumely.gateway.resume.application.command.handler.CommandHandler;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.application.query.data.QueryResult;
import io.zumely.gateway.resume.application.query.handler.QueryHandler;
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
        QueryHandler<T, R> handler = (QueryHandler<T, R>) handlers.get(query.getClass());

        if (handler == null) {
            throw new ApplicationException(
                    "No handler found for query type " + query.getClass().getSimpleName()
            );
        }

        return handler.handle(actor, query);
    }
}
