package com.dolphin.openapi.center.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.inputs.TaskReportDebtsInputs;
import com.dolphin.saas.inputs.TaskReportDetialInputs;
import com.dolphin.saas.service.SonarScanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sonar")
@Api(tags = "质量检测任务", description = "4个")
public class sonarScanController extends MasterCommon {
    @Resource
    private SonarScanService sonarScanService;

    /**
     * 获取扫描任务列表
     *
     * @return
     */
    @ApiOperation("获取扫描任务列表")
    @RequestMapping(value = "/taskLists", method = RequestMethod.POST)
    public Map<String, Object> SonarLists(Long clusterId, String clusterInstanceId) {
        try {
            return JsonResponse(1, sonarScanService.FindReleaseJobs(clusterId, clusterInstanceId), "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 上报扫描结果
     *
     * @return
     */
    @ApiOperation("上报扫描结果")
    @RequestMapping(value = "/taskResults", method = RequestMethod.POST)
    public Map<String, Object> SonarScanResult(Long clusterId, String clusterInstanceId, Long releaseId, TaskReportDebtsInputs taskReportDebtsInputs) {
        try {
            log.info("results: {}", taskReportDebtsInputs);
            sonarScanService.ReprtReleaseStatus(clusterId, clusterInstanceId, releaseId, taskReportDebtsInputs);
            return JsonResponseStr(1, "ok", "上报成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 上报扫描结果mingxi
     *
     * @return
     */
    @ApiOperation("上报扫描结果细节")
    @RequestMapping(value = "/taskResultsItems", method = RequestMethod.POST)
    public Map<String, Object> taskResultsItems(Long clusterId, String clusterInstanceId, Long releaseId, TaskReportDetialInputs taskReportDetialInputs) {
        try {
            sonarScanService.ReprtReleaseDetials(clusterId, clusterInstanceId, releaseId, taskReportDetialInputs);
            return JsonResponseStr(1, "ok", "上报成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 上报任务心跳
     *
     * @return
     */
    @ApiOperation("上报任务心跳")
    @RequestMapping(value = "/taskHeartbeat", method = RequestMethod.POST)
    public Map<String, Object> SonarScanHeartbeat(Long clusterId, String clusterInstanceId, Long releaseId) {
        try {
            sonarScanService.ReportHeartbeat(clusterId, clusterInstanceId, releaseId);
            return JsonResponseStr(1, "ok", "上报成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }
}
