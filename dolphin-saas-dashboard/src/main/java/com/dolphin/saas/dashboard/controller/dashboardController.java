package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.service.DashboardService;
import com.dolphin.saas.service.MemberService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class dashboardController extends MasterCommon {
    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private DashboardService dashboardService;

    @Resource
    private MemberService memberService;

    /**
     * 获取新手任务数据
     *
     * @return
     */
    @ApiOperation("获取新手任务数据")
    @RequestMapping(value = "/newComerTask", method = RequestMethod.POST)
    public Map<String, Object> newComerTask(@RequestHeader Map<String, String> headers) {
        try {
            Map<String, Object> results = memberService.newComerTask(redisUtils.getUUID(headers.get("token")));
            return JsonResponseMap(1, results, "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 获取大盘统计
     *
     * @return
     */
    @ApiOperation("获取大盘统计")
    @RequestMapping(value = "/headerCounts", method = RequestMethod.POST)
    public Map<String, Object> headerCounts(@RequestHeader Map<String, String> headers) {
        try {
            Map<String, Object> results = new HashMap<>();
            String uuid = redisUtils.getUUID(headers.get("token"));
            Map<String, Object> projectCount = dashboardService.ProjectJobs(uuid);
            Map<String, Object> releaseCount = dashboardService.ReleaseJobs(uuid);
            Map<String, Object> releaseTodayCount = dashboardService.ReleaseDayJobs(uuid);
            Map<String, Object> releaseWeekCount = dashboardService.ReleaseWeekJobs(uuid);
            Map<String, Object> releaseRiskCount = dashboardService.ReleaseRiskJobs(uuid);

            results.put("project", projectCount);
            results.put("release", releaseCount);
            results.put("dayRelease", releaseTodayCount);
            results.put("weekRelease", releaseWeekCount);
            results.put("weekRisk", releaseRiskCount);

            return JsonResponseMap(1, results, "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 获取发布统计图（大盘）
     *
     * @return
     */
    @ApiOperation("获取发布统计图")
    @RequestMapping(value = "/bottomCounts", method = RequestMethod.POST)
    public Map<String, Object> bottomCounts(@RequestHeader Map<String, String> headers, String startTime, String endTime) {
        try {
            String uuid = redisUtils.getUUID(headers.get("token"));

            if (startTime == null && endTime == null) {
                // 获取7天的数据
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -7);
                Date monday = c.getTime();
                startTime = sdf.format(monday);
                endTime = sdf.format(new Date().getTime());
            }
            return JsonResponseMap(1, dashboardService.ReleaseAllJobs(startTime, endTime, uuid), "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }
}
