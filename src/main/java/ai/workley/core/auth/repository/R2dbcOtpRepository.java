package ai.workley.core.auth.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface R2dbcOtpRepository extends ReactiveCrudRepository<OtpEntity, Long> {

    Mono<Long> countByEmailAndCreatedAtAfter(String email, Instant after);

    @Query("SELECT * FROM otp_codes WHERE user_id = :userId AND used_at IS NULL ORDER BY created_at DESC LIMIT 1")
    Mono<OtpEntity> findLatestUnusedByUserId(UUID userId);
}
