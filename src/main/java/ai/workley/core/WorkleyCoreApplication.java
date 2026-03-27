package ai.workley.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@EnableAsync
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableR2dbcAuditing
@SpringBootApplication
public class WorkleyCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkleyCoreApplication.class, args);
    }

}
