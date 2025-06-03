package app.awaytogo.gateway.resume.interfaces.web;

import app.awaytogo.gateway.TestRunner;
import app.awaytogo.gateway.resume.dto.CreateResumeApiRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;

import java.util.Map;

public class ResumeCommandControllerTest extends TestRunner {
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
    public void createResume() {
        Map<String, Object> createResumeApiResponse = createResume(new CreateResumeApiRequest("https://linkedin.com/in/victorchicu"));
        Assertions.assertNotNull(createResumeApiResponse);
    }
}