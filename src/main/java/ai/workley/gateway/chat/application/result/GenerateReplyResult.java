package ai.workley.gateway.chat.application.result;

public record GenerateReplyResult() implements Result {
    private static final GenerateReplyResult EMPTY = new GenerateReplyResult();

    public static GenerateReplyResult empty() {
        return EMPTY;
    }
}
