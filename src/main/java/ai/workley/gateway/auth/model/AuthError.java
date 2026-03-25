package ai.workley.gateway.auth.model;

import org.springframework.http.HttpStatus;

public class AuthError extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public AuthError(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getStatus() { return status; }

    public static AuthError invalidEmail() {
        return new AuthError("invalid_email", "Email format is invalid", HttpStatus.BAD_REQUEST);
    }
    public static AuthError passwordTooShort() {
        return new AuthError("password_too_short", "Password must be at least 8 characters", HttpStatus.BAD_REQUEST);
    }
    public static AuthError passwordsMismatch() {
        return new AuthError("passwords_mismatch", "Passwords don't match", HttpStatus.BAD_REQUEST);
    }
    public static AuthError invalidCredentials() {
        return new AuthError("invalid_credentials", "Wrong password", HttpStatus.UNAUTHORIZED);
    }
    public static AuthError invalidOtp() {
        return new AuthError("invalid_otp", "Invalid verification code", HttpStatus.UNAUTHORIZED);
    }
    public static AuthError invalidPreAuth() {
        return new AuthError("invalid_pre_auth", "Session expired, please start over", HttpStatus.UNAUTHORIZED);
    }
    public static AuthError emailAlreadyExists() {
        return new AuthError("email_exists", "Email already registered", HttpStatus.CONFLICT);
    }
    public static AuthError userNotFound() {
        return new AuthError("user_not_found", "User not found", HttpStatus.UNAUTHORIZED);
    }
}
