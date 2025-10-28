package ai.workley.gateway.features.shared.domain.error;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ApplicationErrors {
    public static boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientResponseException wcre) {
            int value = wcre.getStatusCode().value();
            return value == 429 || (value >= 500 && value < 600);
        }
        return throwable instanceof IOException || throwable instanceof TimeoutException;
    }
}
