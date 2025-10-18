package ai.jobbortunity.gateway.chat.domain.event;

import ai.jobbortunity.gateway.chat.application.result.ClassificationResult;

public record ReplyInitiated(String actor, String chatId, ClassificationResult classificationResult, String prompt) implements DomainEvent {

}
