package ai.workley.core.auth.service;

import ai.workley.core.auth.model.AuthenticationError;
import ai.workley.core.auth.repository.OtpEntity;
import ai.workley.core.auth.repository.R2dbcOtpRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class OtpService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_BOUND = 1_000_000;

    private final R2dbcOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final Duration otpExpiry;
    private final int maxAttempts;

    public OtpService(
            @Value("${gateway.otp.expiry:5m}") Duration otpExpiry,
            @Value("${gateway.otp.max-attempts:5}") int maxAttempts,
            PasswordEncoder passwordEncoder,
            R2dbcOtpRepository otpRepository
    ) {
        this.otpExpiry = otpExpiry;
        this.maxAttempts = maxAttempts;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public Mono<Void> verify(UUID userId, String code) {
        return otpRepository.findLatestUnusedByUserId(userId)
                .switchIfEmpty(Mono.error(AuthenticationError.invalidOtp()))
                .flatMap(otp -> {
                    if (otp.getExpiresAt().isBefore(Instant.now())) {
                        return Mono.error(AuthenticationError.otpExpired());
                    }
                    if (otp.getAttempts() >= maxAttempts) {
                        return Mono.error(AuthenticationError.otpMaxAttemptsExceeded());
                    }
                    if (!passwordEncoder.matches(code, otp.getCodeHash())) {
                        otp.setAttempts(otp.getAttempts() + 1);
                        return otpRepository.save(otp)
                                .then(Mono.error(AuthenticationError.invalidOtp()));
                    }
                    otp.setUsedAt(Instant.now());
                    return otpRepository.save(otp).then();
                });
    }

    public Mono<Void> invalidatePending(UUID userId) {
        return otpRepository.findLatestUnusedByUserId(userId)
                .flatMap(otp -> {
                    otp.setUsedAt(Instant.now());
                    return otpRepository.save(otp);
                })
                .then();
    }

    public Mono<Long> countRecentByEmail(String email, Instant after) {
        return otpRepository.countByEmailAndCreatedAtAfter(email, after);
    }

    public Mono<String> create(UUID userId, String email) {
        String code = generateCode();
        String codeHash = passwordEncoder.encode(code);

        OtpEntity entity = new OtpEntity()
                .setUserId(userId)
                .setEmail(email)
                .setCodeHash(codeHash)
                .setAttempts(0)
                .setExpiresAt(Instant.now().plus(otpExpiry))
                .setCreatedAt(Instant.now());

        return otpRepository.save(entity).thenReturn(code);
    }

    private String generateCode() {
        int code = SECURE_RANDOM.nextInt(OTP_BOUND);
        return String.format("%0" + OTP_LENGTH + "d", code);
    }
}
