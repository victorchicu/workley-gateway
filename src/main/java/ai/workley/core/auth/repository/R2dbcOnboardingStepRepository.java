package ai.workley.core.auth.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface R2dbcOnboardingStepRepository extends ReactiveCrudRepository<OnboardingStepEntity, Long> {

    Flux<OnboardingStepEntity> findByUserIdOrderByStepOrder(UUID userId);

    Mono<OnboardingStepEntity> findByUserIdAndStepName(UUID userId, String stepName);
}
