package ai.workley.core.chat.controller;

import ai.workley.core.chat.service.QueryBus;
import ai.workley.core.chat.model.ErrorPayload;
import ai.workley.core.chat.model.GetChat;
import ai.workley.core.chat.model.ApplicationError;
import ai.workley.core.chat.model.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RequestMapping("/api/chats/{chatId}")
@RestController
public class GetChatController {
    private static final Logger log = LoggerFactory.getLogger(GetChatController.class);

    private final QueryBus queryBus;

    public GetChatController(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @GetMapping
    public Mono<ResponseEntity<Payload>> query(Principal actor, @PathVariable String chatId) {
        log.info("Execute get chat query (actor={}, chatId={})", actor.getName(), chatId);

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
