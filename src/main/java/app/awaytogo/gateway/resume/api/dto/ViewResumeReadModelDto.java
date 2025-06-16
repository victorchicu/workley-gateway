package app.awaytogo.gateway.resume.api.dto;

public record ViewResumeReadModelDto(String resumeId) {

    @Override
    public String toString() {
        return "ResumeViewDto{" +
                "resumeId='" + resumeId + '\'' +
                '}';
    }
}
