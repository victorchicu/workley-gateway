package app.awaytogo.gateway.resume.api;

import app.awaytogo.gateway.resume.application.CommandDispatcher;
import app.awaytogo.gateway.resume.domain.command.Response;
import app.awaytogo.gateway.resume.domain.command.impl.SubmitProfileLinkCommand;
import app.awaytogo.gateway.resume.api.dto.SubmitProfileLinkCommandDto;
import app.awaytogo.gateway.resume.api.dto.SubmitProfileLinkResponseDto;
import app.awaytogo.gateway.resume.domain.command.impl.SubmitProfileLinkResponse;
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
    public Mono<ResponseEntity<SubmitProfileLinkResponseDto>> submit(
            Principal principal,
            @Valid @RequestBody SubmitProfileLinkCommandDto submitProfileLinkCommandDto) {
        log.info("Handle command: {}", submitProfileLinkCommandDto);
        SubmitProfileLinkCommand submitProfileLinkCommand = toSubmitProfileLinkCommand(submitProfileLinkCommandDto);
        return commandDispatcher.dispatch(principal, submitProfileLinkCommand)
                .mapNotNull(response -> {
                    return ResponseEntity.created(URI.create(""))
                            .body(toSubmitProfileLinkResponseDto(response));
                })
                .doOnError(error -> {
                    log.error("Failed to submit LinkedIn profile link command: {}",
                            error.getMessage());
                })
                .doOnSuccess(response -> {
                    log.info("LinkedIn profile link submission status code: {}", response.getStatusCode());
                });
    }


    private SubmitProfileLinkCommand toSubmitProfileLinkCommand(
            SubmitProfileLinkCommandDto submitProfileLinkCommandDto) {
        return conversionService.convert(submitProfileLinkCommandDto, SubmitProfileLinkCommand.class);
    }

    private <T extends Response> SubmitProfileLinkResponseDto toSubmitProfileLinkResponseDto(T source) {
        return conversionService.convert(source, SubmitProfileLinkResponseDto.class);
    }
}