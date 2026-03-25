package ai.workley.gateway.auth.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface AuthResponse {
    record ContinueResponse(@JsonProperty("next_step") String nextStep) implements AuthResponse {}
    record StepResponse(@JsonProperty("next_step") String nextStep, @JsonProperty("pre_auth_token") String preAuthToken) implements AuthResponse {}
    record MeResponse(String email) implements AuthResponse {}
    record AuthErrorResponse(String error, String message) implements AuthResponse {}
}
