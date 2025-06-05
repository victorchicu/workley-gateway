package app.awaytogo.gateway.resume.api.mapper;

import app.awaytogo.gateway.resume.api.dto.CreateResumeRequest;
import app.awaytogo.gateway.resume.domain.command.CreateResumeCommand;
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
                .linkedinUrl(request.source())
                .timestamp(Instant.now())
                .build();
    }
}