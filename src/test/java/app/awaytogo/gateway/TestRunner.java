package app.awaytogo.gateway;

import app.awaytogo.gateway.resume.dto.CreateResumeRequest;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@Testcontainers
@EnableTestBinder
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestSpec.class})
public class TestRunner {
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    protected InputDestination sourceDestination;
    @Autowired
    protected OutputDestination targetDestination;

    @BeforeEach
    public void setUp() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofMinutes(5))
                .build();
    }

    protected static String principal(String visitorId) {
        return JWT.create()
                .withSubject(visitorId)
                .withClaim("roles", List.of("VISITOR"))
                .withIssuer("test-issuer")
                .sign(Algorithm.HMAC256("secret"));
    }

    protected <T> T toObject(Message<byte[]> output, Class<T> clazz) throws IOException {
        return objectMapper.readValue(output.getPayload(), clazz);
    }

    protected <T> byte[] toByteArray(T object) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(object);
    }

    protected WebTestClient.ResponseSpec get(String uri, List<Object> args) {
        return webTestClient.get().uri(uri, args.toArray(new Object[0]))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    protected WebTestClient.ResponseSpec get(String token, String uri, List<Object> args) {
        return webTestClient.get().uri(uri, args.toArray(new Object[0]))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    protected WebTestClient.ResponseSpec post(String uri, Object body, List<Object> args) {
        return webTestClient.post().uri(uri, args.toArray(new Object[0]))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    protected WebTestClient.ResponseSpec post(String uri, String token, Object body, List<Object> args) {
        return webTestClient.post().uri(uri, args.toArray(new Object[0]))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .bodyValue(body)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
}