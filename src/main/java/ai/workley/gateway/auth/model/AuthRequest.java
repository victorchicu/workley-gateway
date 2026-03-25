package ai.workley.gateway.auth.model;

public sealed interface AuthRequest {
    record ContinueRequest(String email) implements AuthRequest {}
    record RegisterRequest(String email, String password, String passwordConfirmation) implements AuthRequest {}
    record LoginRequest(String email, String password) implements AuthRequest {}
    record VerifyOtpRequest(String preAuthToken, String otp) implements AuthRequest {}
}
