package ai.workley.gateway.features.chat.api.rest;

import ai.workley.gateway.features.chat.domain.error.ApplicationErrorOutput;
import ai.workley.gateway.features.shared.app.command.results.Output;
import ai.workley.gateway.features.shared.domain.command.Command;
import ai.workley.gateway.features.chat.app.error.ApplicationError;
import ai.workley.gateway.features.chat.app.command.bus.CommandBus;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RequestMapping("/api/command")
@RestController
public class CommandController {

    private final CommandBus commandBus;

    public CommandController(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @PostMapping
    public <T extends Command> Mono<ResponseEntity<Output>> execute(Principal actor, @Valid @RequestBody T command) {
        return commandBus.execute(actor.getName(), command)
                .flatMap((Output output) ->
                        Mono.just(ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(output))
                )
                .onErrorResume(ApplicationError.class,
                        (ApplicationError error) ->
                                Mono.just(ResponseEntity.badRequest()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(new ApplicationErrorOutput(error.getMessage())))
                );
    }
}
