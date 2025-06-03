package app.awaytogo.gateway.resume.interfaces.web;

import app.awaytogo.gateway.resume.command.CreateResumeCommand;
import app.awaytogo.gateway.resume.command.handler.CreateResumeCommandHandler;
import app.awaytogo.gateway.resume.domain.ResumeId;
import app.awaytogo.gateway.resume.domain.ResumeStatus;
import app.awaytogo.gateway.resume.dto.CreateResumeRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class ResumeCommandController {
    private static final Logger log = LoggerFactory.getLogger(ResumeCommandController.class);

    private final CreateResumeCommandHandler createResumeCommandHandler;

    public ResumeCommandController(CreateResumeCommandHandler createResumeCommandHandler) {
        this.createResumeCommandHandler = createResumeCommandHandler;
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> createResume(Principal principal, @Valid @RequestBody CreateResumeRequest createResumeRequest) {

        String resumeId = ResumeId.generate().value();
        log.info("Received request to create resume. New ID: {} for user: {} and LinkedIn URL: {}", resumeId, principal.getName(), createResumeRequest.linkedinUrl());

        CreateResumeCommand command = new CreateResumeCommand(
                resumeId,
                principal.getName(),
                createResumeRequest.linkedinUrl()
        );

        return createResumeCommandHandler.handle(command)
                .then(Mono.fromCallable(() -> {
                            URI location = UriComponentsBuilder.newInstance()
                                    .scheme("https")
                                    .host("awaytogo.app")
                                    .path("/resumes/{id}")
                                    .buildAndExpand(resumeId)
                                    .toUri();
                            log.info("Resume creation initiated. ID: {}. Location: {}", resumeId, location);
                            return ResponseEntity.created(location)
                                    .body(Map.of(
                                            "resumeId", resumeId,
                                            "message", "Resume creation initiated successfully.",
                                            "status", ResumeStatus.PENDING_PROFILE_FETCH.toString()
                                    ));
                        })
                        .onErrorResume(IllegalArgumentException.class, e -> {
                            log.warn("Invalid argument during resume creation for user {}: {}", principal.getName(), e.getMessage());
                            // Consider using ProblemDetail for richer error responses (RFC 7807)
                            return Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Invalid request data: " + e.getMessage())));
                        })
                        .onErrorResume(OptimisticLockingFailureException.class, e -> { // Specific exception from EventStore
                            log.warn("Concurrency issue creating resume {}: {}", resumeId, e.getMessage());
                            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage())));
                        })
                        .onErrorResume(Exception.class, e -> {
                            log.error("Error creating resume for user {}: {}", principal.getName(), e.getMessage(), e);
                            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(Map.of("error", "An unexpected error occurred.")));
                        }));
    }
}