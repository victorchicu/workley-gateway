package ai.workley.core.chat.presentation.rest;

import ai.workley.core.chat.TestRunner;
import ai.workley.core.chat.controller.ChatController;
import ai.workley.core.chat.model.CreateChatPayload;
import ai.workley.core.chat.model.GetChatPayload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class GetChatControllerIT extends TestRunner {
    private static final String API_CHATS_URL = "/api/chats";
    private static final String API_CHAT_URL = "/api/chats/{chatId}";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg17");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Test
    void getChatQuery() {
        EntityExchangeResult<CreateChatPayload> exchange =
                post(new ChatController.CreateChatRequest("I'm Developer"), API_CHATS_URL)
                        .expectStatus().isOk()
                        .expectBody(CreateChatPayload.class)
                        .returnResult();

        CreateChatPayload createResult = exchange.getResponseBody();
        Assertions.assertNotNull(createResult);

        ResponseCookie cookie = exchange.getResponseCookies().getFirst("__HOST-anonymousToken");
        Assertions.assertNotNull(cookie);

        WebTestClient.ResponseSpec getChatSpec =
                get(cookie.getValue(), API_CHAT_URL, createResult.chatId());

        GetChatPayload getChatResult =
                getChatSpec.expectStatus()
                        .isOk()
                        .expectBody(GetChatPayload.class)
                        .returnResult()
                        .getResponseBody();

        Assertions.assertNotNull(getChatResult);
        Assertions.assertNotNull(getChatResult.messages());
        Assertions.assertFalse(getChatResult.messages().isEmpty());
    }
}
