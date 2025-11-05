package ai.workley.gateway.chat.domain.command;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.infrastructure.intent.IntentClassification;

public record GenerateReply(
        String chatId,
        Message<String> prompt,
        IntentClassification classification) implements Command {
}
