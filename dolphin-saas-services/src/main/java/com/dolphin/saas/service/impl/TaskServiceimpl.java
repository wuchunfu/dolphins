package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.ScanTasks;
import com.dolphin.saas.mapper.ScanTasksMapper;
import com.dolphin.saas.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service("taskService")
public class TaskServiceimpl extends BaseTools implements TaskService {
    @Resource
    private ScanTasksMapper scanTasksMapper;

    @Override
    public ScanTasks GetScanReport(String scanId) throws Exception {
        try {
            QueryWrapper<ScanTasks> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("task_id", scanId);
            ScanTasks scanTasks = scanTasksMapper.selectOne(queryWrapper);

            if (scanTasks == null){
                throw new Exception("扫描任务不存在!");
            }
            return scanTasks;
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String CreateScan(String scanTarget, Integer scanType, String scanKey) throws Exception {
        try {
            ScanTasks scanTasks = new ScanTasks();
            scanTasks.setTaskId(UUID.randomUUID().toString());
            scanTasks.setTaskScanTarget(scanTarget);
            scanTasks.setTaskScanKeys(scanKey);
            scanTasks.setTaskScanType(scanType);
            if (scanTasksMapper.insert(scanTasks) < 1){
                throw new Exception("新增扫描任务失败!");
            }
            return scanTasks.getTaskId();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> checkScanStatus(String scanId) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            QueryWrapper<ScanTasks> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("task_id", scanId);
            ScanTasks scanTasks = scanTasksMapper.selectOne(queryWrapper);

            if (scanTasks == null) {
                throw new Exception("扫描任务不存在!");
            }
            results.put("status", scanTasks.getTaskScanStatus());
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return results;
    }
}
