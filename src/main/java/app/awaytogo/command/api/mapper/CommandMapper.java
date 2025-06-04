package app.awaytogo.command.api.mapper;

import app.awaytogo.command.api.dto.CreateResumeRequest;
import app.awaytogo.command.domain.command.CreateResumeCommand;
import org.springframework.stereotype.Component;
import org.springframework.util.IdGenerator;
import org.springframework.util.SimpleIdGenerator;

import java.security.Principal;
import java.time.Instant;

@Component
public class CommandMapper {
    private final IdGenerator idGenerator;

    public CommandMapper() {
        this.idGenerator = new SimpleIdGenerator();
    }

    public CreateResumeCommand toCreateResumeCommand(Principal principal, CreateResumeRequest request) {
        return CreateResumeCommand.builder()
                .resumeId(idGenerator.generateId().toString())
                .userId(principal.getName())
                .linkedinUrl(request.linkedinUrl())
                .timestamp(Instant.now())
                .build();
    }
}