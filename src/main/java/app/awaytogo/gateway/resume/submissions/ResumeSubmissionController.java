package app.awaytogo.gateway.resume.submissions;

import app.awaytogo.gateway.resume.objects.Customer;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/resume/submissions")
public class ResumeSubmissionController {
    private final ConversionService conversionService;

    public ResumeSubmissionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @PostMapping
    public Mono<ResumeSubmissionResult> submit(Principal principal, ResumeSubmissionRequest resumeSubmissionRequest) {
        Customer customer = conversionService.convert(principal, Customer.class);
        return customer.submitResume(resumeSubmissionRequest);
    }

    @GetMapping("/{submissionId}")
    public Mono<ResumeSubmissionResult> findSubmission(Principal principal, @PathVariable String submissionId) {
        Customer customer = conversionService.convert(principal, Customer.class);
        return customer.findResumeSubmission(submissionId);
    }
}
