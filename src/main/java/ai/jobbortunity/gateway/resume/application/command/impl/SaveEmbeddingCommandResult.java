package ai.jobbortunity.gateway.resume.application.command.impl;

import ai.jobbortunity.gateway.resume.application.command.CommandResult;

public record SaveEmbeddingCommandResult() implements CommandResult {
    private static final SaveEmbeddingCommandResult INSTANCE = new SaveEmbeddingCommandResult();

    public static SaveEmbeddingCommandResult empty() {
        return INSTANCE;
    }
}
