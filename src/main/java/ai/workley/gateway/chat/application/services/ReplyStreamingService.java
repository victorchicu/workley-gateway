package ai.workley.gateway.chat.application.services;

import ai.workley.gateway.chat.domain.events.ReplyStarted;
import reactor.core.publisher.Mono;

public interface ReplyStreamingService {

    Mono<Void> on(ReplyStarted e);
}
