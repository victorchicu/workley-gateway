package io.zumely.gateway.resume.interfaces.rest;

import io.zumely.gateway.resume.application.command.data.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

public class CommandControllerIT extends TestRunner {
//    @Container
//    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3-noble");
//
//    @DynamicPropertySource
//    static void mongoDbProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
//    }

    @Test
    void createAnonymousChat() {
        WebTestClient.ResponseSpec spec = post(
                new CreateChatCommand("I'm Java Developer"), "/api/command");

        CreateChatCommandResult actual = spec.expectStatus().isOk()
                .expectBody(CreateChatCommandResult.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(actual);
    }

    @Test
    void addAnonymousMessage() {
        WebTestClient.ResponseSpec createChatSpec = post(
                new CreateChatCommand("I'm Developer"), "/api/command");

        EntityExchangeResult<CreateChatCommandResult> createChatCommandResultEntityExchangeResult = createChatSpec.expectStatus().isOk()
                .expectBody(CreateChatCommandResult.class)
                .returnResult();

        CreateChatCommandResult createChatCommandResult = createChatCommandResultEntityExchangeResult.getResponseBody();

        Assertions.assertNotNull(createChatCommandResult);

        ResponseCookie anonymousTokenCookie =
                createChatCommandResultEntityExchangeResult.getResponseCookies().getFirst("__HOST-anonymousToken");
        Assertions.assertNotNull(anonymousTokenCookie);

        WebTestClient.ResponseSpec addMessageSpec = post(
                anonymousTokenCookie.getValue(),
                new AddMessageCommand(createChatCommandResult.chatId(),
                        Message.valueOf("Java Developer")), "/api/command");

        AddMessageCommandResult addMessageCommandResult = addMessageSpec.expectStatus().isOk()
                .expectBody(AddMessageCommandResult.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(addMessageCommandResult);
    }
}
