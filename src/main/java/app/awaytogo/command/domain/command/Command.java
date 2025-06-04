package app.awaytogo.command.domain.command;

import java.time.Instant;

public interface Command {
    String getResumeId();

    Instant getTimestamp();
}
