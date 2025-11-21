package ai.workley.gateway.chat.infrastructure.web.rest;

import ai.workley.gateway.chat.application.ports.inbound.CommandBus;
import ai.workley.gateway.chat.domain.payloads.Payload;
import ai.workley.gateway.chat.domain.payloads.ErrorPayload;
import ai.workley.gateway.chat.domain.command.Command;
import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.infrastructure.web.rest.idempotency.IdempotencyKey;
import ai.workley.gateway.chat.infrastructure.web.rest.idempotency.IdempotencyKeyContext;
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

    private final CommandBus commandBus;

    public CommandController(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @PostMapping
    @IdempotencyKey
    public <T extends Command> Mono<ResponseEntity<Payload>> execute(Principal actor, @Valid @RequestBody T command) {
        return Mono.deferContextual(contextView -> {
            String idempotencyKey = IdempotencyKeyContext.get(contextView);

            log.info("Execute command (actor={}, command={}, idempotencyKey={})",
                    actor.getName(), command.getClass().getSimpleName(), idempotencyKey);

            return commandBus.execute(actor.getName(), command, idempotencyKey)
                    .flatMap((Payload payload) ->
                            Mono.just(ResponseEntity.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(payload))
                    )
                    .onErrorResume(ApplicationError.class,
                            (ApplicationError error) ->
                                    Mono.just(ResponseEntity.badRequest()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .body(new ErrorPayload(error.getMessage())))
                    );
        });
    }
}
