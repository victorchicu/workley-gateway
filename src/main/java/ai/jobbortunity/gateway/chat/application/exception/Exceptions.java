package ai.jobbortunity.gateway.chat.application.exception;

import com.mongodb.DuplicateKeyException;

public class Exceptions {
    public static boolean isDuplicateKey(Throwable throwable) {
        return throwable instanceof DuplicateKeyException;
    }
}
