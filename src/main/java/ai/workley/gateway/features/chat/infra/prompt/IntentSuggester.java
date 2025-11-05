package ai.workley.gateway.features.chat.infra.prompt;

import ai.workley.gateway.features.chat.domain.Message;
import reactor.core.publisher.Mono;

public interface IntentSuggester {

    Mono<IntentSuggestion> suggest(Message<String> message);
}