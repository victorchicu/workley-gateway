package ai.workley.core.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SendGridProperties {
    private final String apiKey;
    private final String fromEmail;
    private final String fromName;

    public SendGridProperties(
            @Value("${gateway.sendgrid.api-key}") String apiKey,
            @Value("${gateway.sendgrid.from-email}") String fromEmail,
            @Value("${gateway.sendgrid.from-name}") String fromName
    ) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    public String getApiKey() { return apiKey; }
    public String getFromEmail() { return fromEmail; }
    public String getFromName() { return fromName; }
}
