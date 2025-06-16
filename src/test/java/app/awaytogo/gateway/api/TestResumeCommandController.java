package app.awaytogo.gateway.api;

import app.awaytogo.gateway.TestRunner;
import app.awaytogo.gateway.resume.domain.aggregate.ResumeAggregate;
import app.awaytogo.gateway.resume.api.dto.SubmitLinkedInPublicProfileCommandDto;
import app.awaytogo.gateway.resume.api.dto.SubmitLinkedInPublicProfileResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;

import java.util.Collections;

public class TestResumeCommandController extends TestRunner {
    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer("apache/kafka-native:3.8.0");
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.3-noble");

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);
    }

    @Test
    public void submitLinkedPublicProfile() {
        String linkedin = "https://linkedin.com/in/", profileId = "victorchicu";
        SubmitLinkedInPublicProfileResponseDto actual = post("/api/v1/command/resumes", new SubmitLinkedInPublicProfileCommandDto(linkedin.concat(profileId)), Collections.emptyList())
                .expectStatus().isCreated()
                // @formatter:off
                .expectBody(SubmitLinkedInPublicProfileResponseDto.class)
                // @formatter:on
                .returnResult()
                .getResponseBody();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(profileId, actual.getResumeId());
        Assertions.assertEquals(ResumeAggregate.State.INITIATED, actual.getState());
    }
}
