package ai.jobbortunity.gateway.chat.application.intent;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.ai.openai.chat.options")
public class IntentAiChatOptions {
    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}