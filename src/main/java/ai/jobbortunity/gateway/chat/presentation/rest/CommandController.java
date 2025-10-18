package ai.jobbortunity.gateway.chat.presentation.rest;

import ai.jobbortunity.gateway.chat.application.command.Command;
import ai.jobbortunity.gateway.chat.application.bus.CommandBus;
import ai.jobbortunity.gateway.chat.application.result.BadRequestResult;
import ai.jobbortunity.gateway.chat.application.result.Result;
import ai.jobbortunity.gateway.chat.application.error.ApplicationError;
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
    public <T extends Command> Mono<ResponseEntity<Result>> execute(Principal actor, @Valid @RequestBody T command) {
        return commandBus.execute(actor.getName(), command)
                .flatMap((Result result) ->
                        Mono.just(ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(result))
                )
                .onErrorResume(ApplicationError.class,
                        (ApplicationError error) ->
                                Mono.just(ResponseEntity.badRequest()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(new BadRequestResult(error.getMessage())))
                );
    }
}
