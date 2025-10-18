package ai.jobbortunity.gateway.chat.application.result;

public record GenerateReplyResult() implements CommandResult {
    private static final GenerateReplyResult EMPTY = new GenerateReplyResult();

    public static GenerateReplyResult empty() {
        return EMPTY;
    }
}
