package ai.jobbortunity.gateway.chat.infrastructure.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.ai.openai.chat.options")
public class OpenAiChatOptions {
    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
