package io.zumely.gateway.resume.interfaces.rest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@TestConfiguration(proxyBeanMethods = false)
public class TestSpec {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return token -> {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256("secret")).build().verify(token);
            Map<String, String> httpHeaders = toHeaders(jwt);
            return Mono.just(Jwt.withTokenValue(token)
                    .headers(http -> http.putAll(httpHeaders))
                    .claim("sub", jwt.getSubject())
                    .build()
            );
        };
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> toHeaders(DecodedJWT jwt) {
        Map<String, String> headers = null;
        try {
            headers = OBJECT_MAPPER.readValue(new String(Base64.getDecoder().decode(jwt.getHeader())), Map.class);
        } catch (IOException ignored) {

        }
        return headers;
    }
}
