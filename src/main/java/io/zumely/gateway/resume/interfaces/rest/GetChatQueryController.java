package io.zumely.gateway.resume.interfaces.rest;

import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.application.query.InternalErrorQueryResult;
import io.zumely.gateway.resume.application.query.QueryDispatcher;
import io.zumely.gateway.resume.application.query.GetChatQuery;
import io.zumely.gateway.resume.application.query.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RequestMapping("/api/chats/{chatId}")
@RestController
public class GetChatQueryController {
    private static final Logger log = LoggerFactory.getLogger(GetChatQueryController.class);

    private final QueryDispatcher queryDispatcher;

    public GetChatQueryController(QueryDispatcher queryDispatcher) {
        this.queryDispatcher = queryDispatcher;
    }

    @GetMapping
    public Mono<ResponseEntity<QueryResult>> get(Principal actor, @PathVariable String chatId) {
        log.info("Get chat {} for authorId {}",
                chatId, actor.getName());

        return queryDispatcher.dispatch(actor, new GetChatQuery(chatId))
                .flatMap((QueryResult queryResult) ->
                        Mono.just(ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(queryResult))
                )
                .onErrorResume(ApplicationException.class,
                        (ApplicationException error) ->
                                Mono.just(ResponseEntity.badRequest()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(new InternalErrorQueryResult(error.getMessage())))
                );
    }
}