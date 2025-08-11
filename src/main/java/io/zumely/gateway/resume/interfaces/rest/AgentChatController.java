package io.zumely.gateway.resume.interfaces.rest;

import io.zumely.gateway.resume.application.query.QueryDispatcher;
import io.zumely.gateway.resume.application.query.data.GetChatHistoryQuery;
import io.zumely.gateway.resume.application.query.data.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RequestMapping("/api/agent/chats/{chatId}")
@RestController
public class AgentChatController {
    private static final Logger log = LoggerFactory.getLogger(AgentChatController.class);

    private final QueryDispatcher queryDispatcher;

    public AgentChatController(QueryDispatcher queryDispatcher) {
        this.queryDispatcher = queryDispatcher;
    }

    @GetMapping
    public Mono<ResponseEntity<QueryResult>> getChatHistoryQuery(Principal actor, @PathVariable String chatId) {
        log.info("Get chat history {} for actor {}",
                chatId, actor.getName());

        Mono<QueryResult> queryResult = queryDispatcher.dispatch(actor,
                new GetChatHistoryQuery(chatId));

        return queryResult.map((QueryResult result) ->
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(result)
        );
    }
}
