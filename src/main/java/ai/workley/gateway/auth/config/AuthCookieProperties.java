package ai.workley.gateway.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieProperties {
    private final String prefix;

    public AuthCookieProperties(@Value("${gateway.security.auth.cookie.prefix:__HOST-}") String prefix) {
        this.prefix = prefix;
    }

    public String accessTokenCookieName() {
        return prefix + "accessToken";
    }

    public String refreshTokenCookieName() {
        return prefix + "refreshToken";
    }
}
