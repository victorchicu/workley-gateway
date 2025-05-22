package app.awaytogo.gateway.resume;

import app.awaytogo.gateway.common.objects.Profile;
import app.awaytogo.gateway.common.types.ResourceType;

import java.util.Optional;

public interface ResumeImporter {
    boolean supports(ResourceType resourceType);

    Optional<Profile> importFrom(Profile profile);
}
