package io.zumely.gateway.core.security.anonymous;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
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

    private final AnonymousJwtSecret jwtSecret;

    public CookieAnonymousAuthenticationWebFilter(AnonymousJwtSecret jwtSecret) {
        super(UUID.randomUUID().toString());
        this.jwtSecret = jwtSecret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.defer(() ->
                        applyAnonymousSecurityContext(exchange, chain)))
                .flatMap((securityContext) -> chain.filter(exchange));
    }


    private void addAnonymousTokenCookie(ServerWebExchange exchange, String token) {
        ResponseCookie cookie = ResponseCookie.from(ANONYMOUS_TOKEN_COOKIE_KEY, token)
                .path("/")
                .secure(true)
                .maxAge(COOKIE_MAX_AGE)
                .httpOnly(true)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .build();
        exchange.getResponse().addCookie(cookie);
    }

    private String extractAnonymousTokenFromCookie(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(ANONYMOUS_TOKEN_COOKIE_KEY);
        return Optional.ofNullable(cookie)
                .map(HttpCookie::getValue)
                .orElse(null);
    }

    private DecodedJWT createAnonymousJwtToken(ServerWebExchange exchange) {
        String token = JWT.create()
                .withSubject(UUID.randomUUID().toString())
                .withExpiresAt(Instant.now().plus(TOKEN_MAX_AGE))
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
            jwt = JWT.require(jwtSecret.getAlgorithm()).build().verify(token);
        } catch (TokenExpiredException e) {
            jwt = createAnonymousJwtToken(exchange);
        } catch (JWTVerificationException e) {
            throw new InsufficientAuthenticationException("Anonymous token is invalid", e);
        }
        return jwt;
    }

    private Mono<SecurityContext> applyAnonymousSecurityContext(ServerWebExchange exchange, WebFilterChain chain) {
        Authentication authentication =
                createAnonymousAuthentication(exchange);

        SecurityContext securityContext =
                new SecurityContextImpl(authentication);

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                .then(Mono.empty());
    }
}
