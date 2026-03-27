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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.UUID;

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

    @Test
    void createChat_duplicateIdempotencyKey_returnsIdenticalPayload() {
        String idempotencyKey = UUID.randomUUID().toString();

        CreateChatPayload first = webTestClient.post().uri(API_COMMAND_URL)
                .header("Idempotency-Key", idempotencyKey)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateChat("Hello"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateChatPayload.class)
                .returnResult()
                .getResponseBody();

        CreateChatPayload second = webTestClient.post().uri(API_COMMAND_URL)
                .header("Idempotency-Key", idempotencyKey)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateChat("Hello"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateChatPayload.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(first);
        Assertions.assertNotNull(second);
        Assertions.assertEquals(first.chatId(), second.chatId());
        Assertions.assertEquals(first.message().id(), second.message().id());
        Assertions.assertEquals(first.message().content().text(), second.message().content().text());
    }

    @Test
    void addMessage_duplicateIdempotencyKey_returnsIdenticalPayload() {
        EntityExchangeResult<CreateChatPayload> exchange =
                webTestClient.post().uri(API_COMMAND_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(new CreateChat("Setup"))
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(CreateChatPayload.class)
                        .returnResult();

        ResponseCookie cookie = exchange.getResponseCookies().getFirst("__HOST-anonymousToken");
        Assertions.assertNotNull(cookie);

        CreateChatPayload chat = exchange.getResponseBody();
        Assertions.assertNotNull(chat);

        String idempotencyKey = UUID.randomUUID().toString();

        AddMessagePayload first = webTestClient.post().uri(API_COMMAND_URL)
                .cookie("__HOST-anonymousToken", cookie.getValue())
                .header("Idempotency-Key", idempotencyKey)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new AddMessage(chat.chatId(), Message.create(new Text("Test message"))))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AddMessagePayload.class)
                .returnResult()
                .getResponseBody();

        AddMessagePayload second = webTestClient.post().uri(API_COMMAND_URL)
                .cookie("__HOST-anonymousToken", cookie.getValue())
                .header("Idempotency-Key", idempotencyKey)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new AddMessage(chat.chatId(), Message.create(new Text("Test message"))))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AddMessagePayload.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(first);
        Assertions.assertNotNull(second);
        Assertions.assertEquals(first.chatId(), second.chatId());
        Assertions.assertEquals(first.message().id(), second.message().id());
    }
}
