package app.awaytogo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableReactiveMongoAuditing
@EnableReactiveMongoRepositories
@SpringBootApplication
public class AwaytogoGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwaytogoGatewayApplication.class, args);
    }

}
