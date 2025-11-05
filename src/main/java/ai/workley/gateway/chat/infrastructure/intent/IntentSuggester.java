package ai.workley.gateway.chat.infrastructure.intent;

import ai.workley.gateway.chat.domain.Message;
import reactor.core.publisher.Mono;

public interface IntentSuggester {

    Mono<IntentSuggestion> suggest(Message<String> message);
}
