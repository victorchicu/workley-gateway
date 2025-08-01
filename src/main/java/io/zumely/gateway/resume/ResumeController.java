package io.zumely.gateway.resume;

import io.zumely.gateway.resume.objects.HandlePromptRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequestMapping("/api/resumes")
@RestController
public class ResumeController {

    @PostMapping
    public Mono<ResponseEntity<String>> handlePrompt(@RequestBody HandlePromptRequest handlePromptRequest) {
        return Mono.just(ResponseEntity.ok()
                .body(handlePromptRequest.text()));
    }
}