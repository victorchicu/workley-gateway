package app.awaytogo.gateway.resume;

import app.awaytogo.gateway.common.dto.ResumeImportResultDto;
import app.awaytogo.gateway.common.dto.ResumeImportRequestDto;
import app.awaytogo.gateway.common.objects.Profile;
import app.awaytogo.gateway.common.types.ResourceType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeBuilderController {
    private final List<ResumeImporter> importers;

    public ResumeBuilderController(List<ResumeImporter> importers) {
        this.importers = importers;
    }

    @PostMapping("/{resourceType}/import")
    public HttpEntity<ResumeImportResultDto> importResume(@PathVariable ResourceType resourceType, @RequestBody ResumeImportRequestDto requestDto) {
        for (ResumeImporter importer : importers) {
            if (importer.supports(resourceType)) {
                return importer.importFrom(new Profile())
                        .map(p -> ResponseEntity.ok(new ResumeImportResultDto()))
                        .orElseThrow();
            }
        }
        throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
    }
}