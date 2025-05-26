package app.awaytogo.gateway.resume.importers.pdf;

import app.awaytogo.gateway.resume.importers.ResumeImporter;
import app.awaytogo.gateway.common.objects.Profile;
import app.awaytogo.gateway.common.types.ResourceType;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PdfResumeImporter implements ResumeImporter {

    @Override
    public boolean supports(ResourceType resourceType) {
        return ResourceType.PDF.equals(resourceType);
    }

    @Override
    public Optional<Profile> importFrom(Profile profile) {
        throw new UnsupportedOperationException("PDF import not supported yet");
    }
}
