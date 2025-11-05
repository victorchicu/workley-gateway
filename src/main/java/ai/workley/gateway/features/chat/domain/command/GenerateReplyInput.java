package ai.workley.gateway.features.chat.domain.command;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.infra.intent.IntentClassification;
import ai.workley.gateway.features.shared.domain.command.Command;

public record GenerateReplyInput(
        String chatId,
        Message<String> prompt,
        IntentClassification classification) implements Command {
}
