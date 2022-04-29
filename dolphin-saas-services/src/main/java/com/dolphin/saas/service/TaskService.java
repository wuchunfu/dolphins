package com.dolphin.saas.service;

import com.dolphin.saas.entity.ScanTasks;

import java.util.Map;

/**
 * 这个只有工具端才有的服务
 */
public interface TaskService {
    // 获取扫描报告
    ScanTasks GetScanReport(String scanId) throws Exception;

    // 创建扫描任务
    String CreateScan(String scanTarget, Integer scanType, String scanKey) throws Exception;

    // 查询扫描状态
    Map<String, Object> checkScanStatus(String scanId) throws Exception;
}
