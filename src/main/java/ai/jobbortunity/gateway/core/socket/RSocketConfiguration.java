package ai.jobbortunity.gateway.core.socket;

import ai.jobbortunity.gateway.chat.application.command.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class RSocketConfiguration {
    @Bean
    Sinks.Many<Message<String>> chatSink() {
        return Sinks.many().multicast().onBackpressureBuffer(1024, false);
    }
}
