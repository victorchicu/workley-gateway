package ai.workley.core.auth.model;

public sealed interface AuthenticationRequest {
    record LoginRequest(String email, String password) implements AuthenticationRequest {
    }

    record RegisterRequest(String email, String password, String passwordConfirmation) implements AuthenticationRequest {
    }

    record ContinueRequest(String email) implements AuthenticationRequest {
    }

    record VerifyOtpRequest(String preAuthToken, String otp) implements AuthenticationRequest {
    }
}
