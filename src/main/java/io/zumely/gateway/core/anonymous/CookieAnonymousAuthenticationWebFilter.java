package io.zumely.gateway.core.anonymous;

import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CookieAnonymousAuthenticationWebFilter extends AnonymousAuthenticationWebFilter {

    private static final String ANONYMOUS_ID_COOKIE_KEY = "_AID";
    private static final Duration COOKIE_MAX_AGE = Duration.ofDays(30);

    public CookieAnonymousAuthenticationWebFilter() {
        super("anonymousKey");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.defer(() -> {
                    Authentication authentication = createAnonymousAuthentication(exchange);
                    SecurityContext securityContext = new SecurityContextImpl(authentication);
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                            .then(Mono.empty());
                }))
                .flatMap((securityContext) -> chain.filter(exchange));
    }

    private Authentication createAnonymousAuthentication(ServerWebExchange exchange) {
        String anonymousId = extractAnonymousIdFromCookie(exchange);

        if (anonymousId == null) {
            anonymousId = UUID.randomUUID().toString();
            addAnonymousCookie(exchange, anonymousId);
        }

        return new AnonymousAuthenticationToken(
                "anonymousKey",
                anonymousId,
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
    }

    private String extractAnonymousIdFromCookie(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getRequest().getCookies().getFirst(ANONYMOUS_ID_COOKIE_KEY))
                .map(HttpCookie::getValue)
                .orElse(null);
    }

    private void addAnonymousCookie(ServerWebExchange exchange, String anonymousId) {
        ResponseCookie cookie = ResponseCookie.from(ANONYMOUS_ID_COOKIE_KEY, anonymousId)
                .path("/")
                .secure(true)
                .maxAge(COOKIE_MAX_AGE)
                .httpOnly(true)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .build();
        exchange.getResponse().addCookie(cookie);
    }
}