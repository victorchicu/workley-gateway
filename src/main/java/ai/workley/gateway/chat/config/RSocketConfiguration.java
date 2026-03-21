package ai.workley.gateway.chat.config;

import ai.workley.gateway.chat.model.Message;
import ai.workley.gateway.chat.model.Content;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class RSocketConfiguration {
    @Bean
    Sinks.Many<Message<? extends Content>> chatSessionSink() {
        return Sinks.many().multicast().onBackpressureBuffer(1024, false);
    }
}
