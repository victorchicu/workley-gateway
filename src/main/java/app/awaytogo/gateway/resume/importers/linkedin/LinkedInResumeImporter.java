package app.awaytogo.gateway.resume.importers.linkedin;

import app.awaytogo.gateway.resume.importers.ResumeImporter;
import app.awaytogo.gateway.common.objects.Profile;
import app.awaytogo.gateway.common.types.ResourceType;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LinkedInResumeImporter implements ResumeImporter {

    @Override
    public boolean supports(ResourceType resourceType) {
        return ResourceType.LINKEDIN.equals(resourceType);
    }

    @Override
    public Optional<Profile> importFrom(Profile profile) {
        return Optional.empty();
    }
}
