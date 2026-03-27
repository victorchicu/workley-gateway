package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Message;
import ai.workley.core.chat.model.Content;
import ai.workley.core.chat.model.IntentSuggestion;
import reactor.core.publisher.Mono;

public interface IntentSuggester {

    Mono<IntentSuggestion> suggest(Message<? extends Content> message);
}
