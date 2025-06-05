package app.awaytogo.gateway.resume.domain.command;

import java.time.Instant;

public interface Command {
    String getResumeId();

    Instant getTimestamp();
}
