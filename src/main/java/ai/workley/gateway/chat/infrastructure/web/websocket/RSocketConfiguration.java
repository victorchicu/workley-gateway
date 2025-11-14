package ai.workley.gateway.chat.infrastructure.web.websocket;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.TextContent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class RSocketConfiguration {
    @Bean
    Sinks.Many<Message<TextContent>> chatSessionSink() {
        return Sinks.many().multicast().onBackpressureBuffer(1024, false);
    }
}
