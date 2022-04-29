package com.dolphin.saas.commons.clouds.jenkins;

import com.alibaba.fastjson.JSON;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RefV1Api {

    private final String jenkins_url;
    private final String jenkins_username;
    private final String jenkins_password;
    private final JenkinsServer jenkinsServer;
    private final String FilePath;

    public RefV1Api(String jenkins_url, String jenkins_username, String jenkins_password) throws Exception {
        try {
            this.jenkins_url = jenkins_url;
            this.jenkins_username = jenkins_username;
            this.jenkins_password = jenkins_password;

            try {
                File folder = new File("/yaml");
                if (!folder.exists()) {
                    this.FilePath = "yaml";
                } else {
                    this.FilePath = "/yaml";
                }
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

    @Retryable(value = Exception.class, maxAttempts = 5)
    public JenkinsHttpClient getClient() {
        JenkinsHttpClient jenkinsHttpClient = null;
        try {
            jenkinsHttpClient = new JenkinsHttpClient(new URI(this.jenkins_url), this.jenkins_username, this.jenkins_password);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return jenkinsHttpClient;
    }


    /**
     * 创建工程
     *
     * @param JobName 工程名称
     * @throws Exception
     */
    @Retryable(value = Exception.class, maxAttempts = 5)
    public void createJob(String JobName) throws Exception {
        try {
            // 读取文件
            Path path = Paths.get(this.FilePath + "/jenkins-jobs.xml");
            String data = Files.readString(path);
            // 创建工程
            this.jenkinsServer.createJob(JobName, data, true);
        } catch (Exception e) {
            log.error("createJob: " + e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 更新工程信息
     *
     * @param JobName
     * @throws Exception
     */
    @Retryable(value = Exception.class, maxAttempts = 5)
    public void updateJob(String JobName) throws Exception {
        try {
            // 读取文件
            Path path = Paths.get(this.FilePath + "/jenkins-jobs.xml");
            String data = Files.readString(path);
            // 更新工程
            this.jenkinsServer.updateJob(JobName, data, true);
        } catch (Exception e) {
            log.error("createJob: " + e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 判断是否有这个Jenkins工程
     *
     * @param JobName 工程名
     * @return true：有，false：没有
     * @throws Exception
     */
    @Retryable(value = Exception.class, maxAttempts = 5)
    public Boolean hashJob(String JobName) throws Exception {
        try {
            ArrayList<String> JobNameLists = new ArrayList<>();
            Map<String, Job> jobs = this.jenkinsServer.getJobs();
            if (jobs.values().size() > 0) {
                for (Job job : jobs.values()) {
                    JobNameLists.add(job.getName());
                }
                return JobNameLists.contains(JobName);
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("hashJob:" + e);
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
    @Retryable(value = Exception.class, maxAttempts = 5)
    public Long buildJob(String JobName, Map<String, String> paratems) throws Exception {
        try {
            // 获取工程构建
            JobWithDetails job = this.jenkinsServer.getJob(JobName);
            QueueReference build = job.build(paratems, true);

            log.info("getQueueItemUrlPart:" + build.getQueueItemUrlPart());
            QueueItem queueItem = this.jenkinsServer.getQueueItem(build);

            log.info("getUrl:" + queueItem.getUrl());
            while (queueItem.getExecutable() == null) {
                log.info("getExecutable:" + queueItem.getExecutable());
                Thread.sleep(1000);
                queueItem = this.jenkinsServer.getQueueItem(build);
            }
            log.info("getExecutable finish!:" + queueItem.getExecutable().getNumber());
            return queueItem.getExecutable().getNumber();
        } catch (Exception e) {
            log.error("buildJob: " + e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 查构建信息
     *
     * @param JobName
     * @return
     * @throws Exception
     */
    @Retryable(value = Exception.class, maxAttempts = 5)
    public String JobConsoleLog(String JobName) {
        String consoleLog = "";
        try {
            JobWithDetails job = this.jenkinsServer.getJob(JobName);
            BuildWithDetails build = job.getLastBuild().details();
            consoleLog = build.getConsoleOutputText();
        } catch (Exception e) {
            log.error("JobDetails:" + e);
        }
        return consoleLog;
    }

    /**
     * 判断是否已经结束构建
     *
     * @param JobName
     * @return
     * @throws Exception
     */
    @Retryable(value = Exception.class, maxAttempts = 5)
    public Boolean JobStatus(String JobName) throws Exception {
        try {
            JobWithDetails job = this.jenkinsServer.getJob(JobName);
            BuildWithDetails build = job.getLastBuild().details();
            return build.getResult() != null;
        } catch (Exception e) {
            log.error("JobStatus:" + e);
            throw new Exception(e.getMessage());
        }
    }

    /***
     * 获取工程信息
     * @param JobName 工程名
     * @throws Exception
     * 依赖: pipeline-model-declarative-agent
     */
    @Retryable(value = Exception.class, maxAttempts = 5)
    public ArrayList<Map<String, Object>> JobDetials(String JobName, Long buildJobId) throws Exception {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try {
            // 获取pipeline的状态
            String resp = this.getClient().get("/job/" + JobName + "/" + buildJobId + "/wfapi/describe");
            // 获取结果
            Map<String, Object> stagesObject = JSON.parseObject(resp, Map.class);
            List<Map<String, Object>> stagesLists = (List<Map<String, Object>>) stagesObject.get("stages");
            for (Map<String, Object> stageItems : stagesLists) {
                Map<String, Object> items = new HashMap<>();
                items.put("name", stageItems.get("name").toString());
                items.put("time", stageItems.get("durationMillis").toString());
                items.put("status", stageItems.get("status").toString());
                results.add(items);
            }
        } catch (Exception e) {
            log.error("JobDetail:" + e);
            throw new Exception(e.getMessage());
        }
        return results;
    }
}
