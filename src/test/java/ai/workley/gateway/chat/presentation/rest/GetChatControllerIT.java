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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class GetChatControllerIT extends TestRunner {
    private static final String API_COMMAND_URL = "/api/command";
    private static final String API_CHATS_URL = "/api/chats/{chatId}";

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
