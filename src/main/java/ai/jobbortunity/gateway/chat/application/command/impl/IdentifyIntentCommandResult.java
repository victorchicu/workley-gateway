package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandResult;

public record IdentifyIntentCommandResult() implements CommandResult {
    private static final IdentifyIntentCommandResult EMPTY = new IdentifyIntentCommandResult();

    public static IdentifyIntentCommandResult empty() {
        return EMPTY;
    }
}
