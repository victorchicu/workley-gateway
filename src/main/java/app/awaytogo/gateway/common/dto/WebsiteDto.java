package app.awaytogo.gateway.common.dto;

import app.awaytogo.gateway.resume.linkedin.types.WebsiteType;

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
