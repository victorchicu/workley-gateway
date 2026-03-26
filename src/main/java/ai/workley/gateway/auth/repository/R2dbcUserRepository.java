package ai.workley.gateway.auth.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface R2dbcUserRepository extends ReactiveCrudRepository<UserEntity, UUID> {

    Mono<Boolean> existsByEmail(String email);

    Mono<UserEntity> findByEmail(String email);
}