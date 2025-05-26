package app.awaytogo.gateway.resume;

public interface ResumeProfileDraftService {
    void createResume(DraftResume resume);

    DraftResume findResume(String profileId);
}
