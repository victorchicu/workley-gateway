package ai.workley.core.chat.model;

import org.springframework.dao.DataIntegrityViolationException;

public class InfrastructureErrors {
    public static boolean isDuplicateKey(Throwable throwable) {
        return throwable instanceof DataIntegrityViolationException;
    }
}
