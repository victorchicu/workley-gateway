package ai.workley.gateway.chat.infrastructure.web.rest.idempotency;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class IdempotencyKeyFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String idempotencyKey = exchange.getRequest().getHeaders()
                .getFirst(IdempotencyKeyContext.IDEMPOTENCY_HEADER);

        if (idempotencyKey == null) {
            return chain.filter(exchange);
        }

        return chain.filter(exchange)
                .contextWrite(ctx ->
                        ctx.put(IdempotencyKeyContext.IDEMPOTENCY_KEY_CONTEXT_KEY, idempotencyKey)
                );
    }
}