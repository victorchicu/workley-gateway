package io.zumely.gateway.core.socket;

import io.zumely.gateway.resume.application.command.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class SocketConfiguration {
    @Bean
    Sinks.Many<Message<String>> chatSink() {
        return Sinks.many().multicast().onBackpressureBuffer(1024, false);
    }
}