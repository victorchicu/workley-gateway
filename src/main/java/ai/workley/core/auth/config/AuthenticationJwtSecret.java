package ai.workley.core.auth.config;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationJwtSecret {
    private final Algorithm algorithm;

    public AuthenticationJwtSecret(@Value("${gateway.security.auth.jwt.secret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}
