package io.zumely.gateway.core.openai;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GptConfiguration {
    private final String secretKey;

    public GptConfiguration(@Value("${security.openai.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    @Bean
    public OpenAIAsyncClient openAIAsyncClient() {
        return new OpenAIClientBuilder()
                .credential(new KeyCredential(secretKey))
                .buildAsyncClient();
    }
}