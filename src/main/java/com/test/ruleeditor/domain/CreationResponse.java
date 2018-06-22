package com.test.ruleeditor.domain;

/**
 * Created by Srilakshmi on 31/08/17.
 */
public class CreationResponse {

    private String status;

    private  String message;

    private String jobId;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
