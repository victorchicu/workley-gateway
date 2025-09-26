package ai.jobbortunity.gateway.chat.infrastructure.exception;

import com.mongodb.DuplicateKeyException;

public class InfrastructureExceptions {
    public static boolean isDuplicateKey(Throwable throwable) {
        return throwable instanceof DuplicateKeyException;
    }
}
