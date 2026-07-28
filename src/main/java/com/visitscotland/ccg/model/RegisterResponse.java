package com.visitscotland.ccg.model;

public class RegisterResponse {

    private String submissionId;
    private String code;

    public RegisterResponse() {
    }

    public RegisterResponse(String submissionId, String code) {
        this.submissionId = submissionId;
        this.code = code;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
