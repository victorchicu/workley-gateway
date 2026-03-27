package ai.workley.core.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationCookieProperties {
    private final String prefix;

    public AuthenticationCookieProperties(@Value("${gateway.security.auth.cookie.prefix:__HOST-}") String prefix) {
        this.prefix = prefix;
    }

    public String accessTokenCookieName() {
        return prefix + "accessToken";
    }

    public String refreshTokenCookieName() {
        return prefix + "refreshToken";
    }
}
