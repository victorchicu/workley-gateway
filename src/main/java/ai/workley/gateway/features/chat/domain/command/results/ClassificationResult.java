package ai.workley.gateway.features.chat.domain.command.results;

import ai.workley.gateway.features.chat.domain.IntentType;

public record ClassificationResult(IntentType intent, Float confidence) {

}
