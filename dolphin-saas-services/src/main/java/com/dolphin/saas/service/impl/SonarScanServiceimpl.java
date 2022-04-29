package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.*;
import com.dolphin.saas.entity.vo.ImageScanLists;
import com.dolphin.saas.inputs.TaskReportDebtsInputs;
import com.dolphin.saas.inputs.TaskReportDetialInputs;
import com.dolphin.saas.mapper.*;
import com.dolphin.saas.service.SonarScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service("sonarScanService")
public class SonarScanServiceimpl extends BaseTools implements SonarScanService {

    @Resource
    private ClusterMapper clusterMapper;

    @Resource
    private ReleaseStragesMapper releaseStragesMapper;

    @Resource
    private DeployMapper deployMapper;

    @Resource
    private ReleaseDetsMapper releaseDetsMapper;

    @Resource
    private EngineerAnalyzeMapper engineerAnalyzeMapper;

    @Override
    public ArrayList<Map<String, Object>> FindReleaseJobs(Long clusterId, String clusterInstanceId) throws Exception {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            queryWrapper.eq("cluster_instance_id", clusterInstanceId);

            if (clusterMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("不存在这个集群!");
            }

            // 获取集群正在进行发布的任务
            QueryWrapper<Release> querRelease = new QueryWrapper<>();
            querRelease.eq("release_job_cluster_id", clusterId);
            querRelease.eq("release_job_status", 1);

            List<Release> releaseList = deployMapper.selectList(querRelease);

            if (releaseList.size() > 0) {
                for (Release release : releaseList) {
                    // 再判断是否已经完成了质量检测了，只有完成才能暴露
                    QueryWrapper<ReleaseDeployStages> queryWrapper5 = new QueryWrapper<>();
                    queryWrapper5.eq("release_status_name", "质量检测");
                    queryWrapper5.eq("release_id", release.getReleaseId());
                    ReleaseDeployStages releaseDeployStages1 = releaseStragesMapper.selectOne(queryWrapper5);

                    // 判断没有质量检测就跳过, 判断有没有完成，没完成就跳过
                    if (releaseDeployStages1 == null || releaseDeployStages1.getReleaseStatusStages() != 2) {
                        continue;
                    }

                    // 查下聚合器是否开启了，如果没有开启才在列表里
                    QueryWrapper<ReleaseDeployStages> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("release_status_name", "数据聚合器");
                    queryWrapper1.eq("release_id", release.getReleaseId());
                    ReleaseDeployStages releaseDeployStages2 = releaseStragesMapper.selectOne(queryWrapper1);

                    if (releaseDeployStages2 == null || releaseDeployStages2.getReleaseStatusStages() != 0) {
                        continue;
                    }

                    Map<String, Object> items = new HashMap<>();
                    // 模块名
                    String moduleName = "";
                    // 根据ID查类型，如果是Java就查Java
                    QueryWrapper<Release> queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.eq("ht_release_jobs.release_id", release.getReleaseId());
                    queryWrapper2.eq("ht_release_jobs.release_job_cluster_id", clusterId);
                    ImageScanLists imageScanLists = deployMapper.selectAllImageStageRelease(queryWrapper2);

                    if (imageScanLists != null) {
                        items.put("imageName", imageScanLists.getEngineerName() + ":" + imageScanLists.getReleaseVersion());
                        items.put("releaseId", imageScanLists.getReleaseId());
                        items.put("releaseBranch", imageScanLists.getReleaseJobBranch());
                        results.add(items);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return results;
    }

    @Override
    public void ReprtReleaseStatus(Long clusterId, String clusterInstanceId, Long releaseId, TaskReportDebtsInputs taskReportDebtsInputs) throws Exception {
        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            queryWrapper.eq("cluster_instance_id", clusterInstanceId);

            if (clusterMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("不存在这个集群!");
            }

            QueryWrapper<Release> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("release_id", releaseId);
            Release release = deployMapper.selectOne(queryWrapper1);
            if (release == null) {
                throw new Exception("查不到发布任务!");
            }

            // 插入债务
            ReleaseDebts releaseDebts = new ReleaseDebts();
            releaseDebts.setReleaseId(releaseId);
            releaseDebts.setReleaseDebtId(taskReportDebtsInputs.getDebtid());
            releaseDebts.setReleaseDebtName(taskReportDebtsInputs.getDebtname());
            releaseDebts.setReleaseDebtInfo(taskReportDebtsInputs.getAnaysis());
            releaseDebts.setReleaseDebtStar(Float.valueOf(taskReportDebtsInputs.getStar()));
            releaseDebts.setReleaseDebtCreatetime(new Date());

            if (releaseDetsMapper.insert(releaseDebts) < 1) {
                throw new Exception("插入债务失败!");
            }

            // 更新任务状态
            QueryWrapper<ReleaseDeployStages> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("release_status_name", "数据聚合器");
            queryWrapper2.eq("release_id", releaseId);

            ReleaseDeployStages releaseDeployStages = new ReleaseDeployStages();
            releaseDeployStages.setReleaseStatusStages(2);
            if (releaseStragesMapper.update(releaseDeployStages, queryWrapper2) < 1) {
                throw new Exception("更新心跳失败!");
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void ReportHeartbeat(Long clusterId, String clusterInstanceId, Long releaseId) throws Exception {
        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            queryWrapper.eq("cluster_instance_id", clusterInstanceId);

            if (clusterMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("不存在这个集群!");
            }
            // 更新任务状态
            QueryWrapper<ReleaseDeployStages> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("release_status_name", "数据聚合器");
            queryWrapper1.eq("release_id", releaseId);

            ReleaseDeployStages releaseDeployStages = new ReleaseDeployStages();
            releaseDeployStages.setReleaseStatusStages(1);
            if (releaseStragesMapper.update(releaseDeployStages, queryWrapper1) < 1) {
                throw new Exception("更新心跳失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void ReprtReleaseDetials(Long clusterId, String clusterInstanceId, Long releaseId, TaskReportDetialInputs taskReportDetialInputs) throws Exception {
        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            queryWrapper.eq("cluster_instance_id", clusterInstanceId);

            if (clusterMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("不存在这个集群!");
            }

            QueryWrapper<Release> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("release_id", releaseId);
            Release release = deployMapper.selectOne(queryWrapper1);
            if (release == null) {
                throw new Exception("查不到发布任务!");
            }

            EngineerAnalyze engineerAnalyze = new EngineerAnalyze();
            engineerAnalyze.setEngineerCode(taskReportDetialInputs.getCode());
            engineerAnalyze.setEngineerBranch(taskReportDetialInputs.getBranch());
            engineerAnalyze.setEngineerTools("sonar");
            engineerAnalyze.setEngineerRule(taskReportDetialInputs.getRules());
            engineerAnalyze.setEngineerCreatetime(new Date());
            engineerAnalyze.setEngineerCodeLine(taskReportDetialInputs.getLine());
            engineerAnalyze.setEngineerSearchfile(taskReportDetialInputs.getFilePath());
            engineerAnalyze.setEngineerId(release.getReleaseEngineerId());
            engineerAnalyze.setEngineerMessage(taskReportDetialInputs.getMessage());
            engineerAnalyze.setEnginnerVersion(taskReportDetialInputs.getVersion());

            switch (taskReportDetialInputs.getType()) {
                case "CODE_SMELL":
                    engineerAnalyze.setEngineerType(2);
                    break;
                case "BUG":
                    engineerAnalyze.setEngineerType(1);
                    break;
                case "VULNERABILITY":
                    engineerAnalyze.setEngineerType(3);
                    break;
            }

            switch (taskReportDetialInputs.getSeverity()) {
                case "MINOR":
                    engineerAnalyze.setEngineerSeverity(1);
                    break;
                case "MAJOR":
                    engineerAnalyze.setEngineerSeverity(2);
                    break;
                case "CRITICAL":
                    engineerAnalyze.setEngineerSeverity(3);
                    break;
                case "BLOCKER":
                    engineerAnalyze.setEngineerSeverity(4);
                    break;
            }

            if (engineerAnalyzeMapper.insert(engineerAnalyze) < 1) {
                throw new Exception("插入分析结果失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
