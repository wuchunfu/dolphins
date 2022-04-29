package com.dolphin.saas.service;

import java.util.Map;

public interface DashboardService {

    // 工程统计接口 -- 前台API
    Map<String, Object> ProjectJobs(String uuid) throws Exception;

    // 发布统计接口 -- 前台API
    Map<String, Object> ReleaseJobs(String uuid) throws Exception;

    // 今日新发布业务接口 -- 前台API
    Map<String, Object> ReleaseDayJobs(String uuid) throws Exception;

    // 本周新发布业务接口 -- 前台API
    Map<String, Object> ReleaseWeekJobs(String uuid) throws Exception;

    // 本周发现风险接口 -- 前台API
    Map<String, Object> ReleaseRiskJobs(String uuid) throws Exception;

    // 发布总数趋势接口 -- 前台API
    Map<String, Object> ReleaseAllJobs(String StartTime, String EndTime, String uuid) throws Exception;

    // 企业相关数据 -- 后台API
    Map<String, Integer> merchantCount() throws Exception;

    // 订单相关数据 -- 后台API
    Map<String, Integer> orderCount() throws Exception;

    // 集群数 -- 后台API
    Integer clusterCount() throws Exception;

    // 发布数 -- 后台API
    Integer releasesCount() throws Exception;

    // 用户数 -- 后台API
    Integer memberCount() throws Exception;
}
