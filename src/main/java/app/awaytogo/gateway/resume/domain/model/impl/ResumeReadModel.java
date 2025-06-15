package app.awaytogo.gateway.resume.domain.model.impl;

import app.awaytogo.gateway.resume.domain.model.ReadModel;

public class ResumeReadModel implements ReadModel {

    private String userId;
    private String resumeId;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }
}
