package ai.jobbortunity.gateway.chat.presentation.rest;

import ai.jobbortunity.gateway.chat.TestRunner;
import ai.jobbortunity.gateway.chat.application.command.AddMessage;
import ai.jobbortunity.gateway.chat.application.result.AddMessageResult;
import ai.jobbortunity.gateway.chat.application.command.CreateChat;
import ai.jobbortunity.gateway.chat.domain.model.Message;
import ai.jobbortunity.gateway.chat.application.result.CreateChatResult;
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

        CreateChatResult actual = spec.expectStatus().isOk()
                .expectBody(CreateChatResult.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(actual);
    }

    @Test
    void addChatMessage() {
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

        WebTestClient.ResponseSpec addMessageSpec = post(
                cookie.getValue(),
                new AddMessage(createChatResult.chatId(),
                        Message.create("Java Developer")), API_COMMAND_URL);

        AddMessageResult addMessageResult = addMessageSpec.expectStatus().isOk()
                .expectBody(AddMessageResult.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(addMessageResult);
    }
}
