package ai.workley.core.auth.service;

import ai.workley.core.auth.model.OnboardingStepType;
import ai.workley.core.auth.repository.OnboardingStepEntity;
import ai.workley.core.auth.repository.R2dbcOnboardingStepRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class OnboardingService {
    private final R2dbcOnboardingStepRepository onboardingStepRepository;

    public OnboardingService(R2dbcOnboardingStepRepository onboardingStepRepository) {
        this.onboardingStepRepository = onboardingStepRepository;
    }

    public Mono<Boolean> isFullyOnboarded(UUID userId) {
        return findIncompleteSteps(userId)
                .hasElements()
                .map(hasIncomplete -> !hasIncomplete);
    }

    public Flux<OnboardingStepEntity> initializeSteps(UUID userId) {
        OnboardingStepType[] steps = OnboardingStepType.values();
        return Flux.range(0, steps.length)
                .map(i -> new OnboardingStepEntity()
                        .setUserId(userId)
                        .setStepName(steps[i].name())
                        .setStepOrder(i + 1)
                        .setCompleted(false)
                        .setCreatedAt(Instant.now()))
                .concatMap(onboardingStepRepository::save);
    }

    public Mono<OnboardingStepEntity> markStepCompleted(UUID userId, OnboardingStepType step) {
        return onboardingStepRepository.findByUserIdAndStepName(userId, step.name())
                .flatMap(entity -> {
                    entity.setCompleted(true);
                    entity.setCompletedAt(Instant.now());
                    return onboardingStepRepository.save(entity);
                });
    }

    public Flux<OnboardingStepEntity> findIncompleteSteps(UUID userId) {
        return onboardingStepRepository.findByUserIdOrderByStepOrder(userId)
                .filter(step -> !step.isCompleted());
    }

}
