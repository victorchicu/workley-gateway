package ai.workley.gateway.chat.presentation.rest;

import ai.workley.gateway.chat.application.result.BadRequestResult;
import ai.workley.gateway.chat.application.error.ApplicationError;
import ai.workley.gateway.chat.application.bus.QueryBus;
import ai.workley.gateway.chat.application.query.GetChat;
import ai.workley.gateway.chat.application.result.Result;
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
    public Mono<ResponseEntity<Result>> query(Principal actor, @PathVariable String chatId) {
        return queryBus.execute(actor, new GetChat(chatId))
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
