package io.zumely.gateway.resume.interfaces.rest;

import io.zumely.gateway.resume.application.command.prompt.Prompt;
import io.zumely.gateway.resume.application.command.dispatcher.CommandDispatcher;
import io.zumely.gateway.resume.application.command.result.Result;
import io.zumely.gateway.resume.application.service.PromptHandler;
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

@RequestMapping("/api/prompts")
@RestController
public class PromptController {

    private static final Logger log = LoggerFactory.getLogger(PromptController.class);

    private final PromptHandler promptHandler;
    private final CommandDispatcher commandDispatcher;

    public PromptController(PromptHandler promptHandler, CommandDispatcher commandDispatcher) {
        this.promptHandler = promptHandler;
        this.commandDispatcher = commandDispatcher;
    }

    @PostMapping
    public Mono<ResponseEntity<Result>> handlePrompt(@Valid @RequestBody Prompt prompt) {
        log.info("Handle {}", prompt);

        Result result = commandDispatcher.dispatch(promptHandler.handle(prompt));

        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(result)
        );
    }
}
