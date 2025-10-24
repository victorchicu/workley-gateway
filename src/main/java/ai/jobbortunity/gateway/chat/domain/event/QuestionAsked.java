package ai.jobbortunity.gateway.chat.domain.event;

import ai.jobbortunity.gateway.chat.application.result.ClassificationResult;

public record QuestionAsked(String actor, String chatId, String prompt, ClassificationResult classificationResult) implements DomainEvent {

}