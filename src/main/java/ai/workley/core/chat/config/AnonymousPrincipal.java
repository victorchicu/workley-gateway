package ai.workley.core.chat.config;

import java.security.Principal;

public record AnonymousPrincipal(String subject) implements Principal {
    @Override
    public String getName() {
        return subject;
    }
}
