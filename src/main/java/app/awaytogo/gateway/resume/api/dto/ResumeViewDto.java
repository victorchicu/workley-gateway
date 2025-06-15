package app.awaytogo.gateway.resume.api.dto;

public record ResumeViewDto(String resumeId) {

    @Override
    public String toString() {
        return "ResumeViewDto{" +
                "resumeId='" + resumeId + '\'' +
                '}';
    }
}
