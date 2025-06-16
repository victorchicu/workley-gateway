package app.awaytogo.gateway.resume.api;

import app.awaytogo.gateway.resume.application.CommandDispatcher;
import app.awaytogo.gateway.resume.domain.command.Response;
import app.awaytogo.gateway.resume.domain.command.impl.SubmitLinkedInPublicProfileCommand;
import app.awaytogo.gateway.resume.api.dto.SubmitLinkedInPublicProfileCommandDto;
import app.awaytogo.gateway.resume.api.dto.SubmitLinkedInPublicProfileResponseDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/command/resumes")
public class ResumeCommandController {

    private static final Logger log = LoggerFactory.getLogger(ResumeCommandController.class);

    private final ConversionService conversionService;
    private final CommandDispatcher commandDispatcher;

    public ResumeCommandController(ConversionService conversionService, CommandDispatcher commandDispatcher) {
        this.conversionService = conversionService;
        this.commandDispatcher = commandDispatcher;
    }

    @PostMapping
    public Mono<ResponseEntity<SubmitLinkedInPublicProfileResponseDto>> submitLinkedPublicProfile(
            Principal principal,
            @Valid @RequestBody SubmitLinkedInPublicProfileCommandDto submitLinkedInPublicProfileCommandDto) {
        log.info("Handle command: {}", submitLinkedInPublicProfileCommandDto);

        SubmitLinkedInPublicProfileCommand submitLinkedInPublicProfileCommand
                = toSubmitLinkedInPublicProfileCommand(submitLinkedInPublicProfileCommandDto);

        return commandDispatcher.dispatch(principal, submitLinkedInPublicProfileCommand)
                .mapNotNull(response -> {
                    return ResponseEntity.created(URI.create(""))
                            .body(toSubmitLinkedInPublicProfileResponseDto(response));
                })
                .doOnError(error -> {
                    log.error("Failed to submit LinkedIn profile link command: {}",
                            error.getMessage());
                })
                .doOnSuccess(response -> {
                    log.info("LinkedIn profile link submission status code: {}", response.getStatusCode());
                });
    }


    private SubmitLinkedInPublicProfileCommand toSubmitLinkedInPublicProfileCommand(
            SubmitLinkedInPublicProfileCommandDto submitLinkedInPublicProfileCommandDto) {
        return conversionService.convert(submitLinkedInPublicProfileCommandDto, SubmitLinkedInPublicProfileCommand.class);
    }

    private <T extends Response> SubmitLinkedInPublicProfileResponseDto toSubmitLinkedInPublicProfileResponseDto(T source) {
        return conversionService.convert(source, SubmitLinkedInPublicProfileResponseDto.class);
    }
}
