package app.awaytogo.gateway.linkedin;

import app.awaytogo.gateway.linkedin.dto.ResourceImportResultDto;
import app.awaytogo.gateway.linkedin.dto.ResourceImportDto;
import app.awaytogo.gateway.linkedin.types.ResourceType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resumes")
public class ResumeBuilderController {
    @PostMapping("/{resource}/import")
    public HttpEntity<ResourceImportResultDto> importFrom(@PathVariable ResourceType resource, @RequestBody ResourceImportDto dto) {
        return ResponseEntity.ok(new ResourceImportResultDto());
    }
}