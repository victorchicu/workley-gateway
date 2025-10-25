package ai.workley.gateway.chat.presentation.rest;

import ai.workley.gateway.chat.TestRunner;
import ai.workley.gateway.chat.application.command.CreateChat;
import ai.workley.gateway.chat.application.result.CreateChatResult;
import ai.workley.gateway.chat.application.result.GetChatResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

public class GetChatControllerIT extends TestRunner {
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
                new CreateChat("I'm Developer"), API_COMMAND_URL);

        EntityExchangeResult<CreateChatResult> exchange =
                createChatSpec.expectStatus()
                        .isOk()
                        .expectBody(CreateChatResult.class)
                        .returnResult();

        CreateChatResult createChatResult = exchange.getResponseBody();
        Assertions.assertNotNull(createChatResult);

        ResponseCookie cookie = exchange.getResponseCookies().getFirst("__HOST-anonymousToken");
        Assertions.assertNotNull(cookie);

        WebTestClient.ResponseSpec getChatQuerySpec =
                get(cookie.getValue(), API_CHATS_URL, createChatResult.chatId());

        GetChatResult getChatResult =
                getChatQuerySpec.expectStatus()
                        .isOk()
                        .expectBody(GetChatResult.class)
                        .returnResult()
                        .getResponseBody();

        Assertions.assertNotNull(getChatResult);
    }
}
