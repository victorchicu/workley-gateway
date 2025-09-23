package ai.jobbortunity.gateway.resume;

import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@RunWith(SpringRunner.class)
@Testcontainers
@EnableTestBinder
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestSpec.class})
public class TestRunner {
    @Autowired
    protected WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofMinutes(5))
                .build();
    }

    protected WebTestClient.ResponseSpec get(String uri, Object... args) {
        return webTestClient.get().uri(uri, args)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    protected WebTestClient.ResponseSpec get(String anonymousToken, String uri, Object... args) {
        return webTestClient.get().uri(uri, args)
                .cookie("__HOST-anonymousToken", anonymousToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    protected WebTestClient.ResponseSpec post(Object body, String uri, Object... args) {
        return webTestClient.post().uri(uri, args)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    protected WebTestClient.ResponseSpec post(String anonymousToken, Object body, String uri, Object... args) {
        return webTestClient.post().uri(uri, args)
                .cookie("__HOST-anonymousToken", anonymousToken)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
}
