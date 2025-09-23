package ai.jobbortunity.gateway.core.security.anonymous;

import java.security.Principal;

public record AnonymousPrincipal(String subject) implements Principal {
    @Override
    public String getName() {
        return subject;
    }
}
