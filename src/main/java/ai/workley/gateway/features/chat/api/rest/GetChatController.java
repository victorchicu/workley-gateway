package ai.workley.gateway.features.chat.api.rest;

import ai.workley.gateway.features.chat.domain.error.ErrorOutput;
import ai.workley.gateway.features.chat.domain.query.GetChatInput;
import ai.workley.gateway.features.chat.app.error.ApplicationError;
import ai.workley.gateway.features.shared.app.command.results.Output;
import ai.workley.gateway.features.chat.app.query.bus.QueryBus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RequestMapping("/api/chats/{chatId}")
@RestController
public class GetChatController {

    private final QueryBus queryBus;

    public GetChatController(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @GetMapping
    public Mono<ResponseEntity<Output>> query(Principal actor, @PathVariable String chatId) {
        return queryBus.execute(actor, new GetChatInput(chatId))
                .flatMap((Output output) ->
                        Mono.just(ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(output))
                )
                .onErrorResume(ApplicationError.class,
                        (ApplicationError error) ->
                                Mono.just(ResponseEntity.badRequest()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(new ErrorOutput(error.getMessage())))
                );
    }
}
