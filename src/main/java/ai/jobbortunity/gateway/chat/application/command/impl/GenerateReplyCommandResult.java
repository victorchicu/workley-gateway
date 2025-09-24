package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandResult;

public record GenerateReplyCommandResult() implements CommandResult {
    private static final GenerateReplyCommandResult INSTANCE = new GenerateReplyCommandResult();

    public static GenerateReplyCommandResult empty() {
        return INSTANCE;
    }
}
