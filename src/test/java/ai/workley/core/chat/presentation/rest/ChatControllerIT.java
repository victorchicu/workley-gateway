package ai.workley.core.chat.presentation.rest;

import ai.workley.core.chat.TestRunner;
import ai.workley.core.chat.controller.ChatController;
import ai.workley.core.chat.model.AddMessagePayload;
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

public class ChatControllerIT extends TestRunner {
    private static final String API_CHATS_URL = "/api/chats";
    private static final String API_MESSAGES_URL = "/api/chats/{chatId}/messages";

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
                new ChatController.CreateChatRequest("I'm Java Developer"), API_CHATS_URL);

        CreateChatPayload actual = spec.expectStatus().isOk()
                .expectBody(CreateChatPayload.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.chatId());
        Assertions.assertNotNull(actual.message());
    }

    @Test
    void addChatMessage() {
        EntityExchangeResult<CreateChatPayload> exchange =
                post(new ChatController.CreateChatRequest("I'm Developer"), API_CHATS_URL)
                        .expectStatus().isOk()
                        .expectBody(CreateChatPayload.class)
                        .returnResult();

        CreateChatPayload createResult = exchange.getResponseBody();
        Assertions.assertNotNull(createResult);

        ResponseCookie cookie = exchange.getResponseCookies().getFirst("__HOST-anonymousToken");
        Assertions.assertNotNull(cookie);

        WebTestClient.ResponseSpec addMessageSpec = post(
                cookie.getValue(),
                new ChatController.AddMessageRequest("Java Developer"),
                API_MESSAGES_URL, createResult.chatId());

        AddMessagePayload addMessagePayload = addMessageSpec.expectStatus().isOk()
                .expectBody(AddMessagePayload.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(addMessagePayload);
        Assertions.assertNotNull(addMessagePayload.message());
    }

    @Test
    void createChat_duplicateIdempotencyKey_returnsIdenticalPayload() {
        String idempotencyKey = UUID.randomUUID().toString();

        CreateChatPayload first = webTestClient.post().uri(API_CHATS_URL)
                .header("Idempotency-Key", idempotencyKey)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new ChatController.CreateChatRequest("Hello"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateChatPayload.class)
                .returnResult()
                .getResponseBody();

        CreateChatPayload second = webTestClient.post().uri(API_CHATS_URL)
                .header("Idempotency-Key", idempotencyKey)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new ChatController.CreateChatRequest("Hello"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateChatPayload.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(first);
        Assertions.assertNotNull(second);
        Assertions.assertEquals(first.chatId(), second.chatId());
        Assertions.assertEquals(first.message().id(), second.message().id());
    }

    @Test
    void addMessage_duplicateIdempotencyKey_returnsIdenticalPayload() {
        EntityExchangeResult<CreateChatPayload> exchange =
                webTestClient.post().uri(API_CHATS_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(new ChatController.CreateChatRequest("Setup"))
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(CreateChatPayload.class)
                        .returnResult();

        ResponseCookie cookie = exchange.getResponseCookies().getFirst("__HOST-anonymousToken");
        Assertions.assertNotNull(cookie);

        CreateChatPayload chat = exchange.getResponseBody();
        Assertions.assertNotNull(chat);

        String idempotencyKey = UUID.randomUUID().toString();

        AddMessagePayload first = webTestClient.post().uri(API_MESSAGES_URL, chat.chatId())
                .cookie("__HOST-anonymousToken", cookie.getValue())
                .header("Idempotency-Key", idempotencyKey)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new ChatController.AddMessageRequest("Test message"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AddMessagePayload.class)
                .returnResult()
                .getResponseBody();

        AddMessagePayload second = webTestClient.post().uri(API_MESSAGES_URL, chat.chatId())
                .cookie("__HOST-anonymousToken", cookie.getValue())
                .header("Idempotency-Key", idempotencyKey)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new ChatController.AddMessageRequest("Test message"))
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
