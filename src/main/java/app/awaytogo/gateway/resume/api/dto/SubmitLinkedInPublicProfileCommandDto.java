package app.awaytogo.gateway.resume.api.dto;

public record SubmitLinkedInPublicProfileCommandDto(String url) {

    @Override
    public String toString() {
        return "SubmitLinkedInProfileLinkCommandDto{" +
                "url='" + url + '\'' +
                '}';
    }
}
