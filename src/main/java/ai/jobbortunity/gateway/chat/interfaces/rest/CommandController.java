package ai.jobbortunity.gateway.chat.interfaces.rest;

import ai.jobbortunity.gateway.chat.application.command.Command;
import ai.jobbortunity.gateway.chat.application.command.CommandDispatcher;
import ai.jobbortunity.gateway.chat.application.command.InternalErrorCommandResult;
import ai.jobbortunity.gateway.chat.application.command.CommandResult;
import ai.jobbortunity.gateway.chat.application.exception.ApplicationException;
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

    private final CommandDispatcher commandDispatcher;

    public CommandController(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @PostMapping
    public <T extends Command> Mono<ResponseEntity<CommandResult>> execute(Principal actor, @Valid @RequestBody T command) {
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
