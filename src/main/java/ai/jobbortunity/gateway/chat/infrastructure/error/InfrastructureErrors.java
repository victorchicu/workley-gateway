package ai.jobbortunity.gateway.chat.infrastructure.error;

import com.mongodb.DuplicateKeyException;

public class InfrastructureErrors {
    public static boolean isDuplicateKey(Throwable throwable) {
        return throwable instanceof DuplicateKeyException;
    }
}
