package ai.zumely.gateway.resume.domain.query.impl;

import ai.zumely.gateway.resume.domain.query.Query;

public record GetResumeByIdQuery(String resumeId) implements Query {

    @Override
    public String resumeId() {
        return resumeId;
    }
}