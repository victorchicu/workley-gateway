package ai.workley.gateway.chat.presentation.rest.security;

import ai.workley.gateway.chat.presentation.rest.security.anonymous.CookieAnonymousAuthenticationWebFilter;
import ai.workley.gateway.chat.presentation.rest.security.anonymous.AnonymousJwtSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

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
        return (ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchangeSpec) ->
                authorizeExchangeSpec
                        .pathMatchers("/api/command/**", "/api/chats/**", "/actuator/**")
                        .permitAll()
                        .anyExchange()
                        .authenticated();
    }
}
