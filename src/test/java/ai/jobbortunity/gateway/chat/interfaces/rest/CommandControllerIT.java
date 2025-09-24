package ai.jobbortunity.gateway.chat.interfaces.rest;

import ai.jobbortunity.gateway.chat.TestRunner;
import ai.jobbortunity.gateway.chat.application.command.impl.AddMessageCommand;
import ai.jobbortunity.gateway.chat.application.command.impl.AddMessageCommandResult;
import ai.jobbortunity.gateway.chat.application.command.impl.CreateChatCommand;
import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.command.impl.CreateChatCommandResult;
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
                new CreateChatCommand("I'm Java Developer"), API_COMMAND_URL);

        CreateChatCommandResult actual = spec.expectStatus().isOk()
                .expectBody(CreateChatCommandResult.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(actual);
    }

    @Test
    void addChatMessage() {
        WebTestClient.ResponseSpec createChatSpec = post(
                new CreateChatCommand("I'm Developer"), API_COMMAND_URL);

        EntityExchangeResult<CreateChatCommandResult> exchange =
                createChatSpec.expectStatus()
                        .isOk()
                        .expectBody(CreateChatCommandResult.class)
                        .returnResult();

        CreateChatCommandResult createChatCommandResult = exchange.getResponseBody();
        Assertions.assertNotNull(createChatCommandResult);

        ResponseCookie cookie = exchange.getResponseCookies().getFirst("__HOST-anonymousToken");
        Assertions.assertNotNull(cookie);

        WebTestClient.ResponseSpec addMessageSpec = post(
                cookie.getValue(),
                new AddMessageCommand(createChatCommandResult.chatId(),
                        Message.create("Java Developer")), API_COMMAND_URL);

        AddMessageCommandResult addMessageCommandResult = addMessageSpec.expectStatus().isOk()
                .expectBody(AddMessageCommandResult.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(addMessageCommandResult);
    }
}
