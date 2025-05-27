package app.awaytogo.gateway.resume.repository;

import app.awaytogo.gateway.resume.repository.data.ResumeEntity;
import reactor.core.publisher.Mono;

public interface ExtendedResumeRepository {
    Mono<ResumeEntity> findResume(String profileId);
}
