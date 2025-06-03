package app.awaytogo.gateway.resume.command;

import app.awaytogo.gateway.resume.domain.ResumeId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

import java.security.Principal;

public record CreateResumeCommand(
        @NotBlank(message = "Resume ID cannot be blank")
       @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "Resume ID must be a valid UUID")
       String resumeId,

       @NotBlank(message = "User ID cannot be blank")
       String userId,

       @NotBlank(message = "LinkedIn URL cannot be blank")
       @URL(message = "Invalid LinkedIn URL format")
       String linkedinUrl) {

}