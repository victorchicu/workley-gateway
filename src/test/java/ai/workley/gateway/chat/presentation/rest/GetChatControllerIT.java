package ai.workley.gateway.chat.presentation.rest;

import ai.workley.gateway.chat.TestRunner;
import ai.workley.gateway.chat.domain.command.CreateChat;
import ai.workley.gateway.chat.domain.payloads.CreateChatPayload;
import ai.workley.gateway.chat.domain.payloads.GetChatPayload;
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

        EntityExchangeResult<CreateChatPayload> exchange =
                createChatSpec.expectStatus()
                        .isOk()
                        .expectBody(CreateChatPayload.class)
                        .returnResult();

        CreateChatPayload createChatView = exchange.getResponseBody();
        Assertions.assertNotNull(createChatView);

        ResponseCookie cookie = exchange.getResponseCookies().getFirst("__HOST-anonymousToken");
        Assertions.assertNotNull(cookie);

        WebTestClient.ResponseSpec getChatQuerySpec =
                get(cookie.getValue(), API_CHATS_URL, createChatView.chatId());

        GetChatPayload getChatResult =
                getChatQuerySpec.expectStatus()
                        .isOk()
                        .expectBody(GetChatPayload.class)
                        .returnResult()
                        .getResponseBody();

        Assertions.assertNotNull(getChatResult);
    }
}
