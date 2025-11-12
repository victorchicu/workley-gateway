package ai.workley.gateway.chat.infrastructure.web.security;

import ai.workley.gateway.chat.infrastructure.web.rest.anonymous.AnonymousJwtSecret;
import ai.workley.gateway.chat.infrastructure.web.rest.anonymous.CookieAnonymousAuthenticationWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@Configuration
public class SecurityConfiguration {
    private final AnonymousJwtSecret jwtSecret;

    public SecurityConfiguration(AnonymousJwtSecret jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
        return serverHttpSecurity.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .anonymous((ServerHttpSecurity.AnonymousSpec anonymousSpec) ->
                        anonymousSpec.authenticationFilter(
                                new CookieAnonymousAuthenticationWebFilter(this.jwtSecret))
                )
                .authorizeExchange(withAuthorizeExchange())
                .build();
    }

    private static Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> withAuthorizeExchange() {
        String[] endpointsWhitelist = List.of("/api/chats/**", "/api/command/**", "/actuator/**")
                .toArray(new String[0]);
        return (ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchangeSpec) ->
                authorizeExchangeSpec
                        .pathMatchers(endpointsWhitelist)
                        .permitAll()
                        .anyExchange()
                        .authenticated();
    }
}
