package ai.workley.core.chat.presentation.rest;

import ai.workley.core.chat.TestRunner;
import ai.workley.core.chat.model.Message;
import ai.workley.core.chat.model.AddMessage;
import ai.workley.core.chat.model.Text;
import ai.workley.core.chat.model.AddMessagePayload;
import ai.workley.core.chat.model.CreateChat;
import ai.workley.core.chat.model.CreateChatPayload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class CommandControllerIT extends TestRunner {
    private static final String API_COMMAND_URL = "/api/command";

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
    void createChat() {
        WebTestClient.ResponseSpec spec = post(
                new CreateChat("I'm Java Developer"), API_COMMAND_URL);

        CreateChatPayload actual = spec.expectStatus().isOk()
                .expectBody(CreateChatPayload.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(actual);
    }

    @Test
    void addChatMessage() {
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

        WebTestClient.ResponseSpec addMessageSpec = post(
                cookie.getValue(),
                new AddMessage(createChatView.chatId(),
                        Message.create(new Text("Java Developer"))), API_COMMAND_URL);

        AddMessagePayload addMessagePayload = addMessageSpec.expectStatus().isOk()
                .expectBody(AddMessagePayload.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(addMessagePayload);
    }
}
