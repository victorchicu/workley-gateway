package io.zumely.gateway.core.anonymous.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class JwtSecret {
    private final Algorithm algorithm;

    public JwtSecret(@Value("${security.anonymous.jwt.secret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}
