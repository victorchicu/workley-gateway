package ai.workley.gateway.chat.presentation.rest.security.anonymous;

import java.security.Principal;

public record AnonymousPrincipal(String subject) implements Principal {
    @Override
    public String getName() {
        return subject;
    }
}
