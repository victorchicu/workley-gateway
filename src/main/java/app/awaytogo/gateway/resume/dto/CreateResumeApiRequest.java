package app.awaytogo.gateway.resume.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateResumeApiRequest(
        @NotBlank(message = "LinkedIn URL cannot be blank")
        @URL(message = "Invalid LinkedIn URL format. Example: https://www.linkedin.com/in/your_profile")
        String linkedinUrl
) {

}