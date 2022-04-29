package com.dolphin.openapi.center.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.inputs.TaskReportDebtsInputs;
import com.dolphin.saas.inputs.TaskReportImageInputs;
import com.dolphin.saas.service.ImageScanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/imageScan")
@Api(tags = "镜像扫描任务", description = "4个")
public class imageScanController extends MasterCommon {

    @Resource
    private ImageScanService imageScanService;

    /**
     * 获取扫描任务列表
     *
     * @return
     */
    @ApiOperation("获取扫描任务列表")
    @RequestMapping(value = "/taskLists", method = RequestMethod.POST)
    public Map<String, Object> ImageScanLists(Long clusterId, String clusterInstanceId) {
        try {
            return JsonResponse(1, imageScanService.FindReleaseJobs(clusterId, clusterInstanceId), "读取成功!");
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
    public Map<String, Object> ImageScanResult(Long clusterId, String clusterInstanceId, Long releaseId, TaskReportDebtsInputs taskReportDebtsInputs) {
        try {
            imageScanService.ReprtReleaseStatus(clusterId, clusterInstanceId, releaseId, taskReportDebtsInputs);
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
    public Map<String, Object> ImageScanHeartbeat(Long clusterId, String clusterInstanceId, Long releaseId) {
        try {
            imageScanService.ReportHeartbeat(clusterId, clusterInstanceId, releaseId);
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
    public Map<String, Object> taskResultsItems(Long clusterId, String clusterInstanceId, Long releaseId, TaskReportImageInputs taskReportImageInputs) {
        try {
            imageScanService.ReprtReleaseDetials(clusterId, clusterInstanceId, releaseId, taskReportImageInputs);
            return JsonResponseStr(1, "ok", "上报成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }
}
