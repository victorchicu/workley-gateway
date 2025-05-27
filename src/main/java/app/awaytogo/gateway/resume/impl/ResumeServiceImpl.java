package app.awaytogo.gateway.resume.impl;

import app.awaytogo.gateway.resume.Resume;
import app.awaytogo.gateway.resume.repository.ResumeRepository;
import app.awaytogo.gateway.resume.ResumeService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ResumeServiceImpl implements ResumeService {
    private final ResumeRepository resumeRepository;

    public ResumeServiceImpl(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Override
    public void createResume(Resume resume) {

    }

    @Override
    public Mono<Resume> findResume(String profileId) {
        return this.resumeRepository.findResume(profileId)
                .flatMap(resumeEntity -> Mono.just(new Resume()));
    }
}
