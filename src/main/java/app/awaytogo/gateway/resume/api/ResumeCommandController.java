package app.awaytogo.gateway.resume.api;

import app.awaytogo.gateway.resume.api.dto.CommandResponse;
import app.awaytogo.gateway.resume.api.dto.CreateResumeRequest;
import app.awaytogo.gateway.resume.api.mapper.CommandMapper;
import app.awaytogo.gateway.resume.application.service.CommandDispatcher;
import app.awaytogo.gateway.resume.domain.command.CreateResumeCommand;
import app.awaytogo.gateway.resume.domain.enums.ProcessingState;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Principal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeCommandController {
    private static final Logger log = LoggerFactory.getLogger(ResumeCommandController.class);

    private final CommandMapper commandMapper;
    private final CommandDispatcher commandDispatcher;

    public ResumeCommandController(CommandMapper commandMapper, CommandDispatcher commandDispatcher) {
        this.commandMapper = commandMapper;
        this.commandDispatcher = commandDispatcher;
    }

    @PostMapping
    public Mono<ResponseEntity<CommandResponse>> createResume(Principal principal, @Valid @RequestBody CreateResumeRequest request) {
        log.info("Received create resume request for LinkedIn URL: {}", request.source());
        CreateResumeCommand command = commandMapper.toCreateResumeCommand(principal, request);
        return commandDispatcher.dispatch(command)
                .map(aggregateId ->
                        //TODO: Provide host details from configuration and env variables
                        ResponseEntity.created(URI.create("http://localhost:4200/resumes/" + aggregateId))
                                .body(CommandResponse.builder()
                                        .resumeId(command.getResumeId())
                                        .aggregateId(aggregateId)
                                        .processingState(ProcessingState.INITIATED.name())
                                        .message("Resume creation initiated")
                                        .timestamp(Instant.now())
                                        .build())
                )
                .doOnError(error ->
                        log.error("Failed to create resume: {}",
                                error.getMessage())
                )
                .doOnSuccess(response -> {
                    log.info("Resume created: {}", response.getStatusCode());
                });
    }
}