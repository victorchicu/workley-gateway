package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandResult;

public record SaveEmbeddingCommandResult() implements CommandResult {
    private static final SaveEmbeddingCommandResult EMPTY = new SaveEmbeddingCommandResult();

    public static SaveEmbeddingCommandResult empty() {
        return EMPTY;
    }
}
