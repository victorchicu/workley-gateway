package app.awaytogo.gateway.linkedin;

import app.awaytogo.gateway.TestRunner;
import app.awaytogo.gateway.resume.common.dto.ResumeImportRequestDto;
import app.awaytogo.gateway.resume.common.dto.ResumeImportResultDto;
import org.junit.jupiter.api.Test;

public class ResumeControllerIT extends TestRunner {
    @Test
    void testImport() {
        ResumeImportResultDto p =
                importFrom("linkedin",
                        new ResumeImportRequestDto("https://www.linkedin.com/in/victorchicu/"));
    }
}
