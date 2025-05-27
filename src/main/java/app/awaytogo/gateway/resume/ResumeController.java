package app.awaytogo.gateway.resume;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public Mono<Resume> makeDraft(@PathVariable String profileId) {
        return resumeService.findResume(profileId);
    }
}
