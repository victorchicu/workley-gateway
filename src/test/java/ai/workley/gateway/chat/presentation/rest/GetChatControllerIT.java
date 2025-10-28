package ai.workley.gateway.chat.presentation.rest;

import ai.workley.gateway.chat.TestRunner;
import ai.workley.gateway.features.chat.domain.command.CreateChatInput;
import ai.workley.gateway.features.chat.domain.command.CreateChatOutput;
import ai.workley.gateway.features.chat.domain.query.GetChatOutput;
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
                new CreateChatInput("I'm Developer"), API_COMMAND_URL);

        EntityExchangeResult<CreateChatOutput> exchange =
                createChatSpec.expectStatus()
                        .isOk()
                        .expectBody(CreateChatOutput.class)
                        .returnResult();

        CreateChatOutput createChatView = exchange.getResponseBody();
        Assertions.assertNotNull(createChatView);

        ResponseCookie cookie = exchange.getResponseCookies().getFirst("__HOST-anonymousToken");
        Assertions.assertNotNull(cookie);

        WebTestClient.ResponseSpec getChatQuerySpec =
                get(cookie.getValue(), API_CHATS_URL, createChatView.chatId());

        GetChatOutput getChatResult =
                getChatQuerySpec.expectStatus()
                        .isOk()
                        .expectBody(GetChatOutput.class)
                        .returnResult()
                        .getResponseBody();

        Assertions.assertNotNull(getChatResult);
    }
}
