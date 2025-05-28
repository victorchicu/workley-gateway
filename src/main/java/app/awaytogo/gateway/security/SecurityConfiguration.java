package app.awaytogo.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SecurityConfiguration {
    public static final String ANONYMOUS_ID_HEADER_NAME = "X-Anonymous-ID";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
        return serverHttpSecurity.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .anonymous(anonymousSpec -> anonymousSpec
                        .principal("anonymousUser")
                        .authorities("ROLE_ANONYMOUS")
                )
                .requestCache(ServerHttpSecurity.RequestCacheSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        // Permit access to resume submissions (including anonymous users)
                        .pathMatchers("/resume/submissions/**").permitAll()
                        // By default, authenticate any other exchange
                        .anyExchange().authenticated()
                )
                .requestCache(ServerHttpSecurity.RequestCacheSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .build();
    }
}
