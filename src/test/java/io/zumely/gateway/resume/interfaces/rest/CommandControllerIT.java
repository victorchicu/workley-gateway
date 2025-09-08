package io.zumely.gateway.resume.interfaces.rest;

import io.zumely.gateway.resume.TestRunner;
import io.zumely.gateway.resume.application.command.impl.AddChatMessageCommand;
import io.zumely.gateway.resume.application.command.impl.CreateChatCommand;
import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.command.impl.AddChatMessageCommandResult;
import io.zumely.gateway.resume.application.command.impl.CreateChatCommandResult;
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
                new AddChatMessageCommand(createChatCommandResult.chatId(),
                        Message.create("Java Developer")), API_COMMAND_URL);

        AddChatMessageCommandResult addChatMessageCommandResult = addMessageSpec.expectStatus().isOk()
                .expectBody(AddChatMessageCommandResult.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(addChatMessageCommandResult);
    }
}
