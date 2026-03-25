package ai.workley.gateway.auth.service;

import ai.workley.gateway.auth.config.AuthCookieProperties;
import ai.workley.gateway.auth.config.AuthJwtSecret;
import ai.workley.gateway.auth.model.AuthError;
import ai.workley.gateway.auth.model.AuthResponse.*;
import ai.workley.gateway.auth.repository.R2dbcRefreshTokenRepository;
import ai.workley.gateway.auth.repository.R2dbcUserRepository;
import ai.workley.gateway.auth.repository.RefreshTokenEntity;
import ai.workley.gateway.auth.repository.UserEntity;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String HARDCODED_OTP = "1234";
    private static final Duration ACCESS_TOKEN_EXPIRY = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_EXPIRY = Duration.ofDays(30);
    private static final Duration PRE_AUTH_TOKEN_EXPIRY = Duration.ofMinutes(5);
    private static final Duration COOKIE_MAX_AGE_ACCESS = Duration.ofMinutes(15);
    private static final Duration COOKIE_MAX_AGE_REFRESH = Duration.ofDays(30);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final R2dbcUserRepository userRepository;
    private final R2dbcRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthJwtSecret jwtSecret;
    private final AuthCookieProperties cookieProperties;

    public AuthService(R2dbcUserRepository userRepository,
                       R2dbcRefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthJwtSecret jwtSecret,
                       AuthCookieProperties cookieProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecret = jwtSecret;
        this.cookieProperties = cookieProperties;
    }

    public Mono<ContinueResponse> continueAuth(String email) {
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            return Mono.error(AuthError.invalidEmail());
        }
        return userRepository.existsByEmail(normalized)
                .map(exists -> new ContinueResponse(exists ? "login" : "register"));
    }

    public Mono<StepResponse> register(String email, String password, String passwordConfirmation) {
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            return Mono.error(AuthError.invalidEmail());
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return Mono.error(AuthError.passwordTooShort());
        }
        if (!password.equals(passwordConfirmation)) {
            return Mono.error(AuthError.passwordsMismatch());
        }

        UserEntity user = new UserEntity()
                .setEmail(normalized)
                .setPasswordHash(passwordEncoder.encode(password))
                .setCreatedAt(Instant.now())
                .setUpdatedAt(Instant.now());

        return userRepository.existsByEmail(normalized)
                .flatMap(exists -> {
                    if (exists) return Mono.error(AuthError.emailAlreadyExists());
                    return userRepository.save(user);
                })
                .map(saved -> new StepResponse("verify_otp", createPreAuthToken(saved.getId(), saved.getEmail())));
    }

    public Mono<StepResponse> login(String email, String password) {
        String normalized = email.trim().toLowerCase();
        return userRepository.findByEmail(normalized)
                .switchIfEmpty(Mono.error(AuthError.userNotFound()))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                        return Mono.error(AuthError.invalidCredentials());
                    }
                    return Mono.just(new StepResponse("verify_otp",
                            createPreAuthToken(user.getId(), user.getEmail())));
                });
    }

    public Mono<Void> verifyOtp(String preAuthToken, String otp, ServerHttpResponse response) {
        DecodedJWT decoded;
        try {
            decoded = JWT.require(jwtSecret.getAlgorithm()).build().verify(preAuthToken);
        } catch (JWTVerificationException e) {
            return Mono.error(AuthError.invalidPreAuth());
        }

        if (!"pre_auth".equals(decoded.getClaim("stage").asString())) {
            return Mono.error(AuthError.invalidPreAuth());
        }

        if (!HARDCODED_OTP.equals(otp)) {
            return Mono.error(AuthError.invalidOtp());
        }

        String userId = decoded.getSubject();
        String email = decoded.getClaim("email").asString();

        return issueTokens(UUID.fromString(userId), email, response);
    }

    public Mono<Void> logout(String refreshTokenValue, ServerHttpResponse response) {
        clearAuthCookies(response);
        if (refreshTokenValue == null) return Mono.empty();

        String tokenHash = hashToken(refreshTokenValue);
        return refreshTokenRepository.deleteByTokenHash(tokenHash);
    }

    public Mono<String> tryRefreshAccessToken(String accessTokenValue, String refreshTokenValue,
                                               ServerHttpResponse response) {
        if (refreshTokenValue == null) return Mono.empty();

        String tokenHash = hashToken(refreshTokenValue);
        return refreshTokenRepository.findByTokenHash(tokenHash)
                .flatMap(refreshToken -> {
                    if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
                        return refreshTokenRepository.deleteByTokenHash(tokenHash)
                                .then(Mono.<UserEntity>empty());
                    }
                    return userRepository.findById(refreshToken.getUserId());
                })
                .flatMap(user -> {
                    String oldHash = hashToken(refreshTokenValue);
                    return refreshTokenRepository.deleteByTokenHash(oldHash)
                            .then(issueTokensAndReturnAccessToken(user.getId(), user.getEmail(), response));
                });
    }

    private Mono<Void> issueTokens(UUID userId, String email, ServerHttpResponse response) {
        return issueTokensAndReturnAccessToken(userId, email, response).then();
    }

    private Mono<String> issueTokensAndReturnAccessToken(UUID userId, String email, ServerHttpResponse response) {
        String accessToken = JWT.create()
                .withSubject(userId.toString())
                .withClaim("email", email)
                .withClaim("type", "authenticated")
                .withExpiresAt(Instant.now().plus(ACCESS_TOKEN_EXPIRY))
                .sign(jwtSecret.getAlgorithm());

        String refreshTokenValue = UUID.randomUUID().toString();
        String refreshTokenHash = hashToken(refreshTokenValue);

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity()
                .setUserId(userId)
                .setTokenHash(refreshTokenHash)
                .setExpiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRY))
                .setCreatedAt(Instant.now());

        return refreshTokenRepository.save(refreshTokenEntity)
                .doOnSuccess(saved -> {
                    response.addCookie(buildCookie(cookieProperties.accessTokenCookieName(),
                            accessToken, COOKIE_MAX_AGE_ACCESS));
                    response.addCookie(buildCookie(cookieProperties.refreshTokenCookieName(),
                            refreshTokenValue, COOKIE_MAX_AGE_REFRESH));
                })
                .thenReturn(accessToken);
    }

    private ResponseCookie buildCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .maxAge(maxAge)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .build();
    }

    private void clearAuthCookies(ServerHttpResponse response) {
        response.addCookie(ResponseCookie.from(cookieProperties.accessTokenCookieName(), "")
                .path("/").maxAge(0).secure(true).httpOnly(true).build());
        response.addCookie(ResponseCookie.from(cookieProperties.refreshTokenCookieName(), "")
                .path("/").maxAge(0).secure(true).httpOnly(true).build());
    }

    private String createPreAuthToken(UUID userId, String email) {
        return JWT.create()
                .withSubject(userId.toString())
                .withClaim("email", email)
                .withClaim("stage", "pre_auth")
                .withExpiresAt(Instant.now().plus(PRE_AUTH_TOKEN_EXPIRY))
                .sign(jwtSecret.getAlgorithm());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
