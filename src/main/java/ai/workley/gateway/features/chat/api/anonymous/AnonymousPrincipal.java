package ai.workley.gateway.features.chat.api.anonymous;

import java.security.Principal;

public record AnonymousPrincipal(String subject) implements Principal {
    @Override
    public String getName() {
        return subject;
    }
}
