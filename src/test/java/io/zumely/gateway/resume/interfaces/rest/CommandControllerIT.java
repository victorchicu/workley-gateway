package io.zumely.gateway.resume.interfaces.rest;

import io.zumely.gateway.resume.TestRunner;
import io.zumely.gateway.resume.application.command.SendMessageCommand;
import io.zumely.gateway.resume.application.command.CreateChatCommand;
import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.command.SendMessageCommandResult;
import io.zumely.gateway.resume.application.command.CreateChatCommandResult;
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
    void sendMessage() {
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
                new SendMessageCommand(createChatCommandResult.chatId(),
                        Message.create("Java Developer")), API_COMMAND_URL);

        SendMessageCommandResult sendMessageCommandResult = addMessageSpec.expectStatus().isOk()
                .expectBody(SendMessageCommandResult.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(sendMessageCommandResult);
    }
}