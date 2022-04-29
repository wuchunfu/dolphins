package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.*;
import com.dolphin.saas.entity.vo.ImageScanLists;
import com.dolphin.saas.inputs.TaskReportDebtsInputs;
import com.dolphin.saas.inputs.TaskReportImageInputs;
import com.dolphin.saas.mapper.*;
import com.dolphin.saas.service.ImageScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service("imageScanService")
public class ImageScanServiceimpl extends BaseTools implements ImageScanService {

    @Resource
    private ClusterMapper clusterMapper;

    @Resource
    private ReleaseStragesMapper releaseStragesMapper;

    @Resource
    private DeployMapper deployMapper;

    @Resource
    private ReleaseModuleMapper releaseModuleMapper;

    @Resource
    private ReleaseDetsMapper releaseDetsMapper;

    @Resource
    private EngineerContainerAnalyzeMapper engineerContainerAnalyzeMapper;

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
                    // 再判断是否已经完成了镜像构建，只有完成才能暴露
                    QueryWrapper<ReleaseDeployStages> queryWrapper5 = new QueryWrapper<>();
                    queryWrapper5.eq("release_status_name", "构建镜像");
                    queryWrapper5.eq("release_id", release.getReleaseId());
                    ReleaseDeployStages releaseDeployStages1 = releaseStragesMapper.selectOne(queryWrapper5);

                    // 判断没有质量检测就跳过, 判断有没有完成，没完成就跳过
                    if (releaseDeployStages1 == null || releaseDeployStages1.getReleaseStatusStages() != 2) {
                        continue;
                    }

                    // 查下聚合器是否开启了，如果没有开启才在列表里
                    QueryWrapper<ReleaseDeployStages> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("release_status_name", "镜像漏洞扫描");
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
                        // 如果是Java还得判断工程模块
                        if (imageScanLists.getLanguageName().equals("Java")) {
                            QueryWrapper<ReleaseModule> queryWrapper3 = new QueryWrapper<>();
                            queryWrapper3.eq("release_id", release.getReleaseId());
                            ReleaseModule releaseModule = releaseModuleMapper.selectOne(queryWrapper3);
                            if (releaseModule != null) {
                                moduleName = releaseModule.getReleaseModule();
                            } else {
                                moduleName = imageScanLists.getEngineerName();
                            }
                        }
                        items.put("imageName", moduleName + ":" + imageScanLists.getReleaseVersion());
                        items.put("releaseId", imageScanLists.getReleaseId());
                        items.put("nameSpace", release.getReleaseJobNamespace());
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
            QueryWrapper<ReleaseDeployStages> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("release_status_name", "镜像漏洞扫描");
            queryWrapper1.eq("release_id", releaseId);

            ReleaseDeployStages releaseDeployStages = new ReleaseDeployStages();
            releaseDeployStages.setReleaseStatusStages(2);
            if (releaseStragesMapper.update(releaseDeployStages, queryWrapper1) < 1) {
                throw new Exception("更新状态失败!");
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
            queryWrapper1.eq("release_status_name", "镜像漏洞扫描");
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
    public void ReprtReleaseDetials(Long clusterId, String clusterInstanceId, Long releaseId, TaskReportImageInputs taskReportImageInputs) throws Exception {
        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cluster_id", clusterId);
            queryWrapper.eq("cluster_instance_id", clusterInstanceId);

            if (clusterMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("不存在这个集群!");
            }

            EngineerContainerAnalyze engineerContainerAnalyze = new EngineerContainerAnalyze();
            // 获取分析结果
            engineerContainerAnalyze.setReleaseId(releaseId);
            engineerContainerAnalyze.setReleaseVulId(taskReportImageInputs.getVulnerabilityID());
            engineerContainerAnalyze.setReleasePkgName(taskReportImageInputs.getPkgName());
            engineerContainerAnalyze.setReleaseInstalledVersion(taskReportImageInputs.getInstalledVersion());
            engineerContainerAnalyze.setReleaseFixedVersion(taskReportImageInputs.getFixedVersion());
            engineerContainerAnalyze.setReleaseSeverity(taskReportImageInputs.getSeverity());
            engineerContainerAnalyze.setReleaseTitle(taskReportImageInputs.getTitle());
            engineerContainerAnalyze.setReleaseDescription(taskReportImageInputs.getDescription());
            engineerContainerAnalyze.setReleaseReferences(taskReportImageInputs.getReferences());
            engineerContainerAnalyze.setReleaseCreateIime(new Date());

            // 插入到数据库
            if (engineerContainerAnalyzeMapper.insert(engineerContainerAnalyze) < 1) {
                throw new Exception("插入分析结果失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
