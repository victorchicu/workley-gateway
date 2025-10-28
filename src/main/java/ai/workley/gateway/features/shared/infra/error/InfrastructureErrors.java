package ai.workley.gateway.features.shared.infra.error;

import ai.workley.gateway.features.chat.infra.error.InfrastructureError;
import com.mongodb.DuplicateKeyException;

public class InfrastructureErrors {
    public static boolean isDuplicateKey(Throwable throwable) {
        return throwable instanceof DuplicateKeyException;
    }

    public static InfrastructureError runtimeException(String message, Exception cause) {
        return new InfrastructureError(message, cause);
    }
}