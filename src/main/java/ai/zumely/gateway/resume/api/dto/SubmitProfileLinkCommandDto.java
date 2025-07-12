package ai.zumely.gateway.resume.api.dto;

public record SubmitProfileLinkCommandDto(String url) {

    @Override
    public String toString() {
        return "SubmitLinkedInProfileLinkCommandDto{" +
                "url='" + url + '\'' +
                '}';
    }
}
