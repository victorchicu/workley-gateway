package app.awaytogo.gateway.resume.command;

import jakarta.validation.constraints.NotBlank;

import java.security.Principal;

public record CreateResumeCommand(
        @NotBlank(message = "Resume ID cannot be blank") String resumeId,
        @NotBlank(message = "Principal cannot be blank") Principal principal,
        @NotBlank(message = "LinkedIn URL cannot be blank") String linkedinUrl
) {

}
