package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandResult;

public record IdentifyUserIntentCommandResult() implements CommandResult {
    private static final IdentifyUserIntentCommandResult EMPTY = new IdentifyUserIntentCommandResult();

    public static IdentifyUserIntentCommandResult empty() {
        return EMPTY;
    }
}
