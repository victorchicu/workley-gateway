package app.awaytogo.gateway.resume.submissions.converter;

import app.awaytogo.gateway.resume.objects.Customer;
import app.awaytogo.gateway.resume.submissions.ResumeSubmissionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class PrincipalToActorConverter implements Converter<Principal, Customer> {
    private final ResumeSubmissionService resumeSubmissionService;

    public PrincipalToActorConverter(ResumeSubmissionService resumeSubmissionService) {
        this.resumeSubmissionService = resumeSubmissionService;
    }

    @Override
    public Customer convert(Principal source) {
        return new Customer(resumeSubmissionService);
    }
}
