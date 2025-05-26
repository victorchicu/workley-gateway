package app.awaytogo.gateway.resume;

import app.awaytogo.gateway.common.dto.ResumeImportResultDto;
import app.awaytogo.gateway.resume.importers.ResumeImporter;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeProfileDraftController {
    private final List<ResumeImporter> importers;

    public ResumeProfileDraftController(List<ResumeImporter> importers) {
        this.importers = importers;
    }

    @PostMapping("/{profileId}/import")
    public HttpEntity<ResumeImportResultDto> findResume(@PathVariable String profileId) {
        return "";
    }
}
