package io.zumely.gateway.resume.interfaces.rest;

import io.zumely.gateway.resume.TestRunner;
import io.zumely.gateway.resume.application.command.*;
import io.zumely.gateway.resume.application.query.GetChatQueryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

public class GetChatQueryControllerIT extends TestRunner {
    private static final String API_COMMAND_URL = "/api/command";
    private static final String API_CHATS_URL = "/api/chats/{chatId}";

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3-noble");

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void getChatQuery() {
        WebTestClient.ResponseSpec createChatSpec = post(
                new CreateChatCommand("I'm Developer"), API_COMMAND_URL);

        EntityExchangeResult<CreateChatCommandResult> exchange =
                createChatSpec.expectStatus()
                        .isOk()
                        .expectBody(CreateChatCommandResult.class)
                        .returnResult();

        CreateChatCommandResult createChatCommandResult = exchange.getResponseBody();
        Assertions.assertNotNull(createChatCommandResult);

        ResponseCookie cookie = exchange.getResponseCookies().getFirst("__HOST-anonymousToken");
        Assertions.assertNotNull(cookie);

        WebTestClient.ResponseSpec getChatQuerySpec =
                get(cookie.getValue(), API_CHATS_URL, createChatCommandResult.chatId());

        GetChatQueryResult getChatQueryResult =
                getChatQuerySpec.expectStatus()
                        .isOk()
                        .expectBody(GetChatQueryResult.class)
                        .returnResult()
                        .getResponseBody();

        Assertions.assertNotNull(getChatQueryResult);
    }
}