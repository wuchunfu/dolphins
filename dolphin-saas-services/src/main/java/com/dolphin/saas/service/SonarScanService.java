package com.dolphin.saas.service;

import com.dolphin.saas.inputs.TaskReportDebtsInputs;
import com.dolphin.saas.inputs.TaskReportDetialInputs;

import java.util.ArrayList;
import java.util.Map;

public interface SonarScanService {
    // 获取待检测任务
    ArrayList<Map<String, Object>> FindReleaseJobs(Long clusterId, String clusterInstanceId) throws Exception;

    // 上报扫描结果
    void ReprtReleaseStatus(Long clusterId, String clusterInstanceId, Long releaseId, TaskReportDebtsInputs taskReportDebtsInputs) throws Exception;

    // 上报任务心跳
    void ReportHeartbeat(Long clusterId, String clusterInstanceId, Long releaseId) throws Exception;

    // 上报扫描结果细节
    void ReprtReleaseDetials(Long clusterId, String clusterInstanceId, Long releaseId, TaskReportDetialInputs taskReportDetialInputs) throws Exception;
}
