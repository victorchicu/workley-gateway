package ai.workley.core.auth.config;

import ai.workley.core.auth.service.AuthenticationService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.http.HttpCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public class AuthenticatedJwtWebFilter implements WebFilter {
    private final AuthenticationService authenticationService;
    private final AuthenticationJwtSecret jwtSecret;
    private final AuthenticationCookieProperties cookieProperties;

    public AuthenticatedJwtWebFilter(
            AuthenticationService authenticationService,
            AuthenticationJwtSecret jwtSecret,
            AuthenticationCookieProperties cookieProperties
    ) {
        this.jwtSecret = jwtSecret;
        this.cookieProperties = cookieProperties;
        this.authenticationService = authenticationService;
    }

    public record AuthenticatedPrincipal(String userId, String email) implements java.security.Principal {
        @Override
        public String getName() {
            return userId;
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String accessToken = extractCookie(exchange, cookieProperties.accessTokenCookieName());

        if (accessToken == null) {
            return chain.filter(exchange);
        }

        try {
            DecodedJWT jwt = JWT.require(jwtSecret.getAlgorithm()).build().verify(accessToken);
            if (!"authenticated".equals(jwt.getClaim("type").asString())) {
                return chain.filter(exchange);
            }
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                            Mono.just(new SecurityContextImpl(toAuthentication(jwt)))));

        } catch (TokenExpiredException e) {
            // Try silent refresh — returns new access token string if successful
            String refreshToken = extractCookie(exchange, cookieProperties.refreshTokenCookieName());
            return authenticationService.tryRefreshAccessToken(accessToken, refreshToken, exchange.getResponse())
                    .flatMap(newAccessToken -> {
                        // Set authenticated SecurityContext for the current request
                        DecodedJWT newJwt = JWT.require(jwtSecret.getAlgorithm()).build().verify(newAccessToken);
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                        Mono.just(new SecurityContextImpl(toAuthentication(newJwt)))));
                    })
                    .switchIfEmpty(chain.filter(exchange));

        } catch (JWTVerificationException e) {
            return chain.filter(exchange);
        }
    }

    private UsernamePasswordAuthenticationToken toAuthentication(DecodedJWT jwt) {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                jwt.getSubject(), jwt.getClaim("email").asString());
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private String extractCookie(ServerWebExchange exchange, String name) {
        return Optional.ofNullable(exchange.getRequest().getCookies().getFirst(name))
                .map(HttpCookie::getValue)
                .orElse(null);
    }
}
