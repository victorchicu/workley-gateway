package app.awaytogo.gateway.linkedin;

import app.awaytogo.gateway.TestRunner;
import app.awaytogo.gateway.common.dto.ResumeImportRequestDto;
import app.awaytogo.gateway.common.dto.ResumeImportResultDto;
import org.junit.jupiter.api.Test;

public class ResumeProfileDraftControllerIT extends TestRunner {
    @Test
    void testImport() {
        ResumeImportResultDto p =
                importFrom("linkedin",
                        new ResumeImportRequestDto("https://www.linkedin.com/in/victorchicu/"));
    }
}
