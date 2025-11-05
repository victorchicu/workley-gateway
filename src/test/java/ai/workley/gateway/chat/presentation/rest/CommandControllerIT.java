package ai.workley.gateway.chat.presentation.rest;

import ai.workley.gateway.chat.TestRunner;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.command.AddMessage;
import ai.workley.gateway.chat.domain.payloads.AddMessagePayload;
import ai.workley.gateway.chat.domain.command.CreateChat;
import ai.workley.gateway.chat.domain.payloads.CreateChatPayload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

public class CommandControllerIT extends TestRunner {
    private static final String API_COMMAND_URL = "/api/command";

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3-noble");

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
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
                        Message.create("Java Developer")), API_COMMAND_URL);

        AddMessagePayload addMessageView = addMessageSpec.expectStatus().isOk()
                .expectBody(AddMessagePayload.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(addMessageView);
    }
}
