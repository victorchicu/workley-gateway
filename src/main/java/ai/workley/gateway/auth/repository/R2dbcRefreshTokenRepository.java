package ai.workley.gateway.auth.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface R2dbcRefreshTokenRepository extends ReactiveCrudRepository<RefreshTokenEntity, UUID> {

    Mono<Void> deleteByUserId(UUID userId);

    Mono<Void> deleteByTokenHash(String tokenHash);

    Mono<RefreshTokenEntity> findByTokenHash(String tokenHash);
}