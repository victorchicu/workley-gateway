package io.zumely.gateway.resume;

import io.zumely.gateway.resume.objects.ProcessingTask;
import io.zumely.gateway.resume.tasks.impl.AsyncTaskResponse;
import io.zumely.gateway.resume.objects.PromptValueRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequestMapping("/api/resumes")
@RestController
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);

    @PostMapping
    public Mono<ResponseEntity<AsyncTaskResponse<ProcessingTask>>> createFromPrompt(@RequestBody PromptValueRequest promptValueRequest) {
        log.info("Create resume from prompt: {}", promptValueRequest);
        return Mono.just(
                ResponseEntity.ok()
                        .body(new AsyncTaskResponse<>(UUID.randomUUID().toString(),
                                new ProcessingTask("Creating your resume from the provided prompt. This may take a few moments...")))
        );
    }
}