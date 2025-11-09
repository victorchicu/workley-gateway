package ai.workley.gateway.chat.application.ports.outbound;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.infrastructure.intent.IntentSuggestion;
import reactor.core.publisher.Mono;

public interface IntentSuggester {

    Mono<IntentSuggestion> suggest(Message<String> message);
}
