package com.clouds.dolphins.base.kubernetes;

import lombok.Data;

@Data
public class JenkinsIntity {
    private String jenkinsPassword;
    private String sonarToken;
    private String gitlabToken;
    private String tcrUserName;
    private String tcrToken;
    private String sonarServerUrl;
    private String k8sApiServer;
    private String gitlabPassword;
}
