package ai.workley.core.auth.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface AuthenticationResponse {
    record MeResponse(String email) implements AuthenticationResponse {
    }

    record StepResponse(
            @JsonProperty("next_step") String nextStep,
            @JsonProperty("pre_auth_token") String preAuthToken)
            implements AuthenticationResponse {
    }

    record ContinueResponse(
            @JsonProperty("next_step") String nextStep)
            implements AuthenticationResponse {
    }

    record AuthenticationErrorResponse(
            String error, String message)
            implements AuthenticationResponse {
    }

    record OnboardingIncompleteResponse
            (String error, String message, @JsonProperty("next_step") String nextStep)
            implements AuthenticationResponse {
    }
}
