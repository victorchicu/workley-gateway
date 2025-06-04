package app.awaytogo.command.api.controller;

import app.awaytogo.command.api.dto.CommandResponse;
import app.awaytogo.command.api.dto.CreateResumeRequest;
import app.awaytogo.command.api.mapper.CommandMapper;
import app.awaytogo.command.application.service.CommandDispatcher;
import app.awaytogo.command.domain.command.CreateResumeCommand;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/commands/resumes")
public class CommandController {
    private static final Logger log = LoggerFactory.getLogger(CommandController.class);

    private final CommandMapper commandMapper;
    private final CommandDispatcher commandDispatcher;

    public CommandController(CommandMapper commandMapper, CommandDispatcher commandDispatcher) {
        this.commandMapper = commandMapper;
        this.commandDispatcher = commandDispatcher;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<CommandResponse> createResume(Principal principal, @Valid @RequestBody CreateResumeRequest request) {
        log.info("Received create resume request for LinkedIn URL: {}", request.linkedinUrl());

        CreateResumeCommand command = commandMapper.toCreateResumeCommand(principal, request);

        return commandDispatcher.dispatch(command)
                .map(resumeId ->
                        CommandResponse.builder()
                                .commandId(command.getResumeId())
                                .aggregateId(resumeId)
                                .status("ACCEPTED")
                                .message("Resume creation initiated")
                                .timestamp(Instant.now())
                                .build()
                )
                .doOnSuccess(response ->
                        log.info("Resume creation command accepted with ID: {}",
                                response.getAggregateId()))
                .doOnError(error ->
                        log.error("Failed to create resume: {}",
                                error.getMessage())
                );
    }
}