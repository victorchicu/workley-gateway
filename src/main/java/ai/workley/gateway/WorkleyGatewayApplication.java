package ai.workley.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@EnableAsync
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableReactiveMongoAuditing
@EnableReactiveMongoRepositories
@SpringBootApplication
public class WorkleyGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkleyGatewayApplication.class, args);
    }

}
