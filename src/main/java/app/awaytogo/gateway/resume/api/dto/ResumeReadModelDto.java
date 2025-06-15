package app.awaytogo.gateway.resume.api.dto;

public record ResumeReadModelDto(String resumeId) {

    @Override
    public String toString() {
        return "ResumeViewDto{" +
                "resumeId='" + resumeId + '\'' +
                '}';
    }
}
