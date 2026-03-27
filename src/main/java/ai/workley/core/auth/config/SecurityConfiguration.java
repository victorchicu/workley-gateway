package ai.workley.core.auth.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

import java.util.List;

@Configuration
public class SecurityConfiguration {
    private final AnonymousJwtSecret jwtSecret;
    private final AuthenticatedJwtWebFilter authenticatedJwtWebFilter;

    public SecurityConfiguration(AnonymousJwtSecret jwtSecret,
                                  AuthenticatedJwtWebFilter authenticatedJwtWebFilter) {
        this.jwtSecret = jwtSecret;
        this.authenticatedJwtWebFilter = authenticatedJwtWebFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
        return serverHttpSecurity.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .anonymous((ServerHttpSecurity.AnonymousSpec anonymousSpec) ->
                        anonymousSpec.authenticationFilter(
                                new CookieAnonymousAuthenticationWebFilter(this.jwtSecret))
                )
                .addFilterBefore(authenticatedJwtWebFilter, SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION)
                .authorizeExchange(withAuthorizeExchange())
                .build();
    }

    private static Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> withAuthorizeExchange() {
        String[] endpointsWhitelist =
                List.of("/api/chats/**", "/api/command/**", "/api/auth/**", "/actuator/**")
                        .toArray(new String[0]);
        return (ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchangeSpec) ->
                authorizeExchangeSpec
                        .pathMatchers(endpointsWhitelist)
                        .permitAll()
                        .anyExchange()
                        .authenticated();
    }
}
