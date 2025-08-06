package io.zumely.gateway.core.anonymous;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.zumely.gateway.core.anonymous.jwt.JwtSecret;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.authentication.AnonymousAuthenticationWebFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CookieAnonymousAuthenticationWebFilter extends AnonymousAuthenticationWebFilter {

    private static final String ANONYMOUS_KEY = "anonymousId";
    private static final String ANONYMOUS_TOKEN_COOKIE_KEY = "__HOST-anonymousToken";
    private static final Duration TOKEN_MAX_AGE = Duration.ofMinutes(5);
    private static final Duration COOKIE_MAX_AGE = Duration.ofDays(30);

    private final JwtSecret jwtSecret;

    public CookieAnonymousAuthenticationWebFilter(JwtSecret jwtSecret) {
        super(UUID.randomUUID().toString());
        this.jwtSecret = jwtSecret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.defer(() -> {
                    Authentication authentication =
                            createAnonymousAuthentication(exchange);

                    SecurityContext securityContext =
                            new SecurityContextImpl(authentication);

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                            .then(Mono.empty());
                }))
                .flatMap((securityContext) -> chain.filter(exchange));
    }


    private void addAnonymousCookie(ServerWebExchange exchange, String anonymousId) {
        ResponseCookie cookie = ResponseCookie.from(ANONYMOUS_TOKEN_COOKIE_KEY, anonymousId)
                .path("/")
                .secure(true)
                .maxAge(COOKIE_MAX_AGE)
                .httpOnly(true)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .build();
        exchange.getResponse().addCookie(cookie);
    }

    private String extractAnonymousToken(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(ANONYMOUS_TOKEN_COOKIE_KEY);
        return Optional.ofNullable(cookie)
                .map(HttpCookie::getValue)
                .orElse(null);
    }

    private Authentication createAnonymousAuthentication(ServerWebExchange exchange) {
        String token = extractAnonymousToken(exchange);
        DecodedJWT jwt;
        if (token == null) {
            token = JWT.create()
                    .withSubject(UUID.randomUUID().toString())
                    .withExpiresAt(Instant.now().plus(TOKEN_MAX_AGE))
                    .sign(jwtSecret.getAlgorithm());

            addAnonymousCookie(exchange, token);

            jwt = JWT.require(jwtSecret.getAlgorithm())
                    .build()
                    .verify(token);
        } else {
            try {
                jwt = JWT.require(jwtSecret.getAlgorithm())
                        .build()
                        .verify(token);
            } catch (TokenExpiredException e) {
                token = JWT.create()
                        .withSubject(UUID.randomUUID().toString())
                        .withExpiresAt(Instant.now().plus(TOKEN_MAX_AGE))
                        .sign(jwtSecret.getAlgorithm());

                addAnonymousCookie(exchange, token);

                jwt = JWT.require(jwtSecret.getAlgorithm())
                        .build()
                        .verify(token);
            }
        }

        if (jwt == null) {
            throw new InsufficientAuthenticationException("JWT token is invalid");
        }

        return new AnonymousAuthenticationToken(
                ANONYMOUS_KEY,
                new AnonymousPrincipal(jwt.getSubject()),
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    }

    public record AnonymousPrincipal(String subject) implements Principal {
        @Override
            public String getName() {
                return subject;
            }
        }
}
