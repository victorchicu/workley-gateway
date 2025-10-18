package ai.jobbortunity.gateway;

import ai.jobbortunity.gateway.chat.infrastructure.config.props.OpenAiChatOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableReactiveMongoAuditing
@EnableReactiveMongoRepositories
@EnableConfigurationProperties({OpenAiChatOptions.class})
@SpringBootApplication
public class JobbortunityGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobbortunityGatewayApplication.class, args);
    }

}
