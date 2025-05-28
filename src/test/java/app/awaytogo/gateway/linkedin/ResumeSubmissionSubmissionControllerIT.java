package app.awaytogo.gateway.linkedin;

import app.awaytogo.gateway.TestRunner;
import app.awaytogo.gateway.resume.submissions.ResumeSubmissionRequest;
import app.awaytogo.gateway.resume.dto.ResumeImportResultDto;
import org.junit.jupiter.api.Test;

public class ResumeSubmissionSubmissionControllerIT extends TestRunner {
    @Test
    void testImport() {
        ResumeImportResultDto p =
                importFrom("linkedin",
                        new ResumeSubmissionRequest("https://www.linkedin.com/in/victorchicu/"));
    }
}
