package ai.workley.gateway.chat.api.rest.anonymous;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.authentication.AnonymousAuthenticationWebFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CookieAnonymousAuthenticationWebFilter extends AnonymousAuthenticationWebFilter {

    private static final String ANONYMOUS_KEY = "anonymousId";
    private static final String ANONYMOUS_TOKEN_COOKIE = "__HOST-anonymousToken";
    private static final Duration COOKIE_MAX_AGE = Duration.ofDays(30);
    private static final Duration TOKEN_EXPIRES_THRESHOLD = Duration.ofMinutes(5);
    private static final Duration TOKEN_REFRESH_THRESHOLD = Duration.ofMinutes(1);

    private final AnonymousJwtSecret jwtSecret;

    public CookieAnonymousAuthenticationWebFilter(AnonymousJwtSecret jwtSecret) {
        super(ANONYMOUS_KEY);
        this.jwtSecret = jwtSecret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.defer(() ->
                        ReactiveSecurityContextHolder.getContext()
                                .switchIfEmpty(Mono.fromSupplier(() ->
                                        new SecurityContextImpl(createAnonymousAuthentication(exchange)))))
                .flatMap(context -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))));
    }


    private void addAnonymousTokenCookie(ServerWebExchange exchange, String token) {
        ResponseCookie cookie = ResponseCookie.from(ANONYMOUS_TOKEN_COOKIE, token)
                .path("/")
                .secure(true)
                .maxAge(COOKIE_MAX_AGE)
                .httpOnly(true)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .build();
        exchange.getResponse().addCookie(cookie);
    }

    private String extractAnonymousTokenFromCookie(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(ANONYMOUS_TOKEN_COOKIE);
        return Optional.ofNullable(cookie)
                .map(HttpCookie::getValue)
                .orElse(null);
    }

    private DecodedJWT createAnonymousJwtToken(ServerWebExchange exchange) {
        String token = JWT.create()
                .withSubject(UUID.randomUUID().toString())
                .withExpiresAt(Instant.now().plus(TOKEN_EXPIRES_THRESHOLD))
                .sign(jwtSecret.getAlgorithm());

        addAnonymousTokenCookie(exchange, token);

        return JWT.require(jwtSecret.getAlgorithm()).build().verify(token);
    }

    private Authentication createAnonymousAuthentication(ServerWebExchange exchange) {
        String token = extractAnonymousTokenFromCookie(exchange);

        DecodedJWT jwt = token == null
                ? createAnonymousJwtToken(exchange)
                : maybeRefreshAnonymousJwtToken(exchange, token);

        return new AnonymousAuthenticationToken(ANONYMOUS_KEY,
                new AnonymousPrincipal(jwt.getSubject()), List.of(
                new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    }

    private DecodedJWT maybeRefreshAnonymousJwtToken(ServerWebExchange exchange, String token) {
        DecodedJWT jwt;
        try {
            jwt = JWT.require(jwtSecret.getAlgorithm()).build()
                    .verify(token);

            Instant expiresAt = Optional.ofNullable(jwt.getExpiresAt())
                    .map(Date::toInstant)
                    .orElse(null);

            if (expiresAt == null
                    || expiresAt.isBefore(Instant.now().plus(TOKEN_REFRESH_THRESHOLD))) {
                jwt = createAnonymousJwtToken(exchange);
            }

        } catch (JWTVerificationException e) {
            jwt = createAnonymousJwtToken(exchange);
        }
        return jwt;
    }
}