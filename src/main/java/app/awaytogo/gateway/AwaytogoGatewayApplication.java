package app.awaytogo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableReactiveMongoAuditing
@EnableReactiveMongoRepositories
//@EnableConfigurationProperties({GatewayProperties.class, InboxProperties.class, GptProperties.class})
@SpringBootApplication
public class AwaytogoGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwaytogoGatewayApplication.class, args);
    }

}
