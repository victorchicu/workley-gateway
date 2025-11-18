package ai.workley.gateway.chat.application.ports.outbound.intent;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.domain.content.TextContent;
import ai.workley.gateway.chat.domain.intent.IntentSuggestion;
import reactor.core.publisher.Mono;

public interface IntentSuggester {

    Mono<IntentSuggestion> suggest(Message<? extends Content> message);
}
