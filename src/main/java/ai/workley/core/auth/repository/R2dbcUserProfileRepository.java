package ai.workley.core.auth.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface R2dbcUserProfileRepository extends ReactiveCrudRepository<UserProfileEntity, Long> {

    Mono<UserProfileEntity> findByUserId(UUID userId);

    Mono<Boolean> existsByUserId(UUID userId);
}
