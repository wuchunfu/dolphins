package com.clouds.dolphins.base.jenkins;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class RefV2Api {
    private final String jenkins_url;
    private final String jenkins_username;
    private final String jenkins_password;
    private final JenkinsServer jenkinsServer;

    public RefV2Api(String jenkins_url, String jenkins_username, String jenkins_password) throws Exception {
        try {
            this.jenkins_url = jenkins_url;
            this.jenkins_username = jenkins_username;
            this.jenkins_password = jenkins_password;

            try {
                this.jenkinsServer = new JenkinsServer(new URI(this.jenkins_url), this.jenkins_username, this.jenkins_password);
            } catch (URISyntaxException e) {
                log.error("RefV1Api.JenkinsServer:" + e);
                throw new Exception(e.getMessage());
            }
        } catch (Exception e) {
            log.error("RefV1Api.init: " + e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 创建工程
     *
     * @param JobName 工程名称
     * @throws Exception
     */
    public void createJob(String JobName) throws Exception {
        try {
            // 读取文件
            Path path = Paths.get("yaml/jenkins-jobs.xml");
            String data = Files.readString(path);
            // 创建工程
            this.jenkinsServer.createJob(JobName, data, true);
        } catch (Exception e) {
            log.error("createJob: " + e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 构建工程
     *
     * @param JobName  工程名称
     * @param paratems 参数
     * @throws Exception
     */
    public void buildJob(String JobName, Map<String, String> paratems) throws Exception {
        try {
            // 获取工程构建
            JobWithDetails job = this.jenkinsServer.getJob(JobName);
            job.build(paratems, true);
        } catch (Exception e) {
            log.error("buildJob: " + e);
            throw new Exception(e.getMessage());
        }
    }
}
