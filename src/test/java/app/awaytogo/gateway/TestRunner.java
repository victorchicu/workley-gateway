package app.awaytogo.gateway;

import app.awaytogo.gateway.resume.common.dto.ResumeImportRequestDto;
import app.awaytogo.gateway.resume.common.dto.ResumeImportResultDto;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestSpec.class})
public class TestRunner {
    @Autowired
    protected WebTestClient webTestClient;

    protected ResumeImportResultDto importFrom(String resource, ResumeImportRequestDto dto) {
        return post("/api/resumes/{resource}/import", dto, List.of(resource))
                .expectStatus().isOk()
                .expectBody(ResumeImportResultDto.class)
                .returnResult()
                .getResponseBody();
    }

    protected WebTestClient.ResponseSpec post(String uri, Object body, List<Object> args) {
        return webTestClient.post().uri(uri, args.toArray(new Object[0]))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
}
