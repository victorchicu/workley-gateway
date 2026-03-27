package ai.workley.core.auth.model;

import org.springframework.http.HttpStatus;

public class AuthenticationError extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public AuthenticationError(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getStatus() { return status; }

    public static AuthenticationError invalidEmail() {
        return new AuthenticationError("invalid_email", "Email format is invalid", HttpStatus.BAD_REQUEST);
    }
    public static AuthenticationError passwordTooShort() {
        return new AuthenticationError("password_too_short", "Password must be at least 8 characters", HttpStatus.BAD_REQUEST);
    }
    public static AuthenticationError passwordsMismatch() {
        return new AuthenticationError("passwords_mismatch", "Passwords don't match", HttpStatus.BAD_REQUEST);
    }
    public static AuthenticationError invalidCredentials() {
        return new AuthenticationError("invalid_credentials", "Wrong password", HttpStatus.UNAUTHORIZED);
    }
    public static AuthenticationError invalidOtp() {
        return new AuthenticationError("invalid_otp", "Invalid verification code", HttpStatus.UNAUTHORIZED);
    }
    public static AuthenticationError invalidPreAuth() {
        return new AuthenticationError("invalid_pre_auth", "Session expired, please start over", HttpStatus.UNAUTHORIZED);
    }
    public static AuthenticationError emailAlreadyExists() {
        return new AuthenticationError("email_exists", "Email already registered", HttpStatus.CONFLICT);
    }
    public static AuthenticationError userNotFound() {
        return new AuthenticationError("user_not_found", "User not found", HttpStatus.UNAUTHORIZED);
    }
}
