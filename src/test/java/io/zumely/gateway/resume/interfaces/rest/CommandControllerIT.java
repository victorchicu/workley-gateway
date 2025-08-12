package io.zumely.gateway.resume.interfaces.rest;

import io.zumely.gateway.resume.application.command.data.CreateChatCommand;
import io.zumely.gateway.resume.application.command.data.CreateChatCommandResult;
import io.zumely.gateway.resume.application.command.data.Prompt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

public class CommandControllerIT extends TestRunner {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3-noble");

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void createChatCommand() {
        WebTestClient.ResponseSpec spec = post(
                new CreateChatCommand(
                        new Prompt("I'm Java Developer")), "/api/agent/command");

        CreateChatCommandResult actual = spec.expectStatus().isOk()
                .expectBody(CreateChatCommandResult.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(actual);
    }
}
