package io.zumely.gateway.resume.interfaces.rest;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.command.dispatcher.CommandDispatcher;
import io.zumely.gateway.resume.application.command.result.Result;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RequestMapping("/api/agent")
@RestController
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final CommandDispatcher commandDispatcher;

    public AgentController(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @PostMapping("/command")
    public <T extends Command> Mono<ResponseEntity<Result>> handleCommand(Principal actor, @Valid @RequestBody T command) {
        log.info("Handle {}", command);

        Result result = commandDispatcher.dispatch(actor, command);

        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(result)
        );
    }
}