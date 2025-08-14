package io.zumely.gateway.resume.interfaces.rest;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.command.CommandDispatcher;
import io.zumely.gateway.resume.application.command.InternalErrorCommandResult;
import io.zumely.gateway.resume.application.command.CommandResult;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RequestMapping("/api/command")
@RestController
public class CommandController {

    private static final Logger log = LoggerFactory.getLogger(CommandController.class);

    private final CommandDispatcher commandDispatcher;

    public CommandController(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @PostMapping
    public <T extends Command> Mono<ResponseEntity<CommandResult>> executeCommand(Principal actor, @Valid @RequestBody T command) {
        log.info("Handle {}", command);

        return commandDispatcher.dispatch(actor, command)
                .flatMap((CommandResult commandResult) ->
                        Mono.just(ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(commandResult))
                )
                .onErrorResume(ApplicationException.class,
                        (ApplicationException error) ->
                                Mono.just(ResponseEntity.badRequest()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(new InternalErrorCommandResult(error.getMessage())))
                );
    }
}