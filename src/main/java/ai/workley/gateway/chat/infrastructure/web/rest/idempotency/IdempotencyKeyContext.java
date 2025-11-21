package ai.workley.gateway.chat.infrastructure.web.rest.idempotency;

import reactor.util.context.Context;
import reactor.util.context.ContextView;

public class IdempotencyKeyContext {
    public static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    public static final String IDEMPOTENCY_KEY_CONTEXT_KEY = "idempotencyKey";

    private IdempotencyKeyContext() {
    }

    public static String get(ContextView contextView) {
        if (contextView.hasKey(IDEMPOTENCY_KEY_CONTEXT_KEY)) {
            return contextView.get(IDEMPOTENCY_KEY_CONTEXT_KEY);
        }
        return null;
    }

    public static Context save(Context context, String idempotencyKey) {
        if (idempotencyKey == null) {
            return context;
        }
        return context.put(IDEMPOTENCY_KEY_CONTEXT_KEY, idempotencyKey);
    }
}