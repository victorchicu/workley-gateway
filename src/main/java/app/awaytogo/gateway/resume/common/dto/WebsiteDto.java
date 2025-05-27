package app.awaytogo.gateway.resume.common.dto;

import app.awaytogo.gateway.resume.importers.linkedin.types.WebsiteType;

public class WebsiteDto {
    private String url;
    private WebsiteType type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public WebsiteType getType() {
        return type;
    }

    public void setType(WebsiteType type) {
        this.type = type;
    }
}
