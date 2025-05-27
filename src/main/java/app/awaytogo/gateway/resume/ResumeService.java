package app.awaytogo.gateway.resume;

import reactor.core.publisher.Mono;

public interface ResumeService {

    void createResume(Resume resume);

    Mono<Resume> findResume(String profileId);
}
