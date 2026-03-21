package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.Message;
import ai.workley.gateway.chat.model.Content;
import ai.workley.gateway.chat.model.IntentSuggestion;
import reactor.core.publisher.Mono;

public interface IntentSuggester {

    Mono<IntentSuggestion> suggest(Message<? extends Content> message);
}
