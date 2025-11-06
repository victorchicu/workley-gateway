package ai.workley.gateway.chat.api.rest;

import ai.workley.gateway.chat.domain.payloads.ErrorPayload;
import ai.workley.gateway.chat.domain.query.GetChat;
import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.domain.payloads.Payload;
import ai.workley.gateway.chat.application.query.QueryBus;
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
    public Mono<ResponseEntity<Payload>> query(Principal actor, @PathVariable String chatId) {
        return queryBus.execute(actor, new GetChat(chatId))
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
    }
}
