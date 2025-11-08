package ai.workley.gateway.chat.domain;

public enum IntentType {
    FIND_JOB(
            // High Confidence
            """
            The user is looking for job opportunities. 
            Help them explore roles that match their experience and interests. 
            Ask one short clarifying question at a time if needed (e.g., roles, experience level, location, preferences).
            Keep a natural conversational tone.
            """,
            // Low Confidence
            """
            The user might be interested in job opportunities, but it's unclear.
            Gently confirm whether they're job searching, without sounding formal or scripted.
            Keep the question short and natural.
            Example: "Are you exploring new roles right now?".
            """
    ),
    FIND_TALENT(
            // High Confidence
            """
            The user is looking to hire someone. 
            Help clarify what role they're hiring for and what type of candidate fits. 
            Ask one short, practical question at a time. 
            Keep the tone conversational and efficient.
            """,
            // Low Confidence
            """
            The user might be looking to hire, but it's unclear.
            Ask a casual confirmation question.
            Example: "Are you looking to bring someone onto your team?".
            Keep it short and friendly.
            """
    ),
    CREATE_RESUME(
            // High Confidence
            """
            The user wants help with their resume or professional profile.
            Ask about their background one piece at a time (role, experience, skills, achievements).
            Give clear, specific, supportive suggestions.
            Keep sentences short and conversational.
            """,
            // Low Confidence
            """
            The user might need resume help, but it's uncertain.
            Gently confirm by asking a short, natural question.
            Example: "Are you working on your resume right now?".
            Be supportive and relaxed.
            """
    ),
    UNRELATED(
            // High Confidence
            """
            Continue the conversation naturally without steering toward job search or hiring unless the user brings it up.
            Keep tone friendly, clear, and conversational.
            """,
            // Low Confidence
            """
            Continue the conversation naturally and keep responses relaxed and friendly.
            """
    );

    private final String highConfidencePrompt;
    private final String lowConfidencePrompt;

    IntentType(String highConfidencePrompt, String lowConfidencePrompt) {
        this.highConfidencePrompt = highConfidencePrompt;
        this.lowConfidencePrompt = lowConfidencePrompt;
    }

    public String getSystemPrompt(Float confidence) {
        if (confidence != null && confidence < 0.7f) {
            return lowConfidencePrompt;
        }
        return highConfidencePrompt;
    }
}
