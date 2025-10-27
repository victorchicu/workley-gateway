package ai.workley.gateway.features.chat.api.rest;

import ai.workley.gateway.features.chat.domain.query.GetChat;
import ai.workley.gateway.features.chat.domain.error.ApplicationError;
import ai.workley.gateway.features.shared.app.command.results.Result;
import ai.workley.gateway.features.chat.domain.command.BadRequestResult;
import ai.workley.gateway.features.chat.infra.eventbus.QueryBus;
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
