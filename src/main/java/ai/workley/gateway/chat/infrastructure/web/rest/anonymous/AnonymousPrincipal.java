package ai.workley.gateway.chat.infrastructure.web.rest.anonymous;

import java.security.Principal;

public record AnonymousPrincipal(String subject) implements Principal {
    @Override
    public String getName() {
        return subject;
    }
}
