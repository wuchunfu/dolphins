package com.dolphin.saas.jobs;

import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.KuberContor;
import com.dolphin.saas.commons.clouds.jenkins.RefV1Api;
import com.dolphin.saas.commons.clouds.tencent.entity.DolphinsDeployment;
import com.dolphin.saas.commons.clouds.tencent.entity.KubIntity;
import com.dolphin.saas.entity.KubBaseConfig;
import com.dolphin.saas.entity.KubConfigYaml;
import com.dolphin.saas.entity.ReleaseModule;
import com.dolphin.saas.entity.ServiceDeployWorkConfig;
import com.dolphin.saas.entity.vo.ReleaseExecItems;
import com.dolphin.saas.entity.vo.ReleaseStagesLists;
import com.dolphin.saas.service.ClusterService;
import com.dolphin.saas.service.DeployService;
import com.dolphin.saas.service.EngineeringService;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ReleaseScheduler extends BaseTools {
    @Autowired
    private RedisCommonUtils redisCommonUtils;

    @Resource
    private DeployService deployService;

    @Resource
    private ClusterService clusterService;

    @Resource
    private EngineeringService engineeringService;

    /**
     * 执行发布任务
     */
    @Scheduled(cron = "*/1 * * * * ?")
    public void Jobs() {
        try {
            ArrayList<ReleaseExecItems> releaseArrayList = deployService.getReleaseLists();
            for (ReleaseExecItems release : releaseArrayList) {
                /*
                 *  变量库
                 */
                String JenkinsBuild = "JenkinsBuildIdJobs." + DigestUtils.md5DigestAsHex(release.toString().getBytes());
                String TaskLock = "buildRelease." + DigestUtils.md5DigestAsHex(release.toString().getBytes());
                ReleaseModule releaseModule = null;
                String DockerfileURL = null;
                ServiceDeployWorkConfig ClusterAPIInfo = null, DockerInfo = null, JenkinsInfo = null, SonarInfo = null;
                Boolean releaseJobStatus = true;

                // 判断这把基础的锁在不在，如果不在则
                if (this.redisCommonUtils.hasKeys(TaskLock)) {
                    continue;
                }

                // 初始化幂等锁
                this.redisCommonUtils.noExpireSset(TaskLock, 1);
//                log.info("[初始化幂等锁] 工程名称:{}, 幂等锁:{}", release.getEngineerName(), TaskLock);

                try {
                    // 获取所有基础配置
                    DockerfileURL = deployService.getDockerfileURL(release.getEngineerDockerfileId());
                    JenkinsInfo = clusterService.getClusterConf(release.getReleaseJobClusterId(), "Jenkins服务");
                    SonarInfo = clusterService.getClusterConf(release.getReleaseJobClusterId(), "sonar服务");
                    ClusterAPIInfo = clusterService.getClusterConf(release.getReleaseJobClusterId(), "K8S集群服务");
                    DockerInfo = clusterService.getClusterConf(release.getReleaseJobClusterId(), "docker仓库");
                    // 看看是否是Java模块化发布
                    releaseModule = deployService.findReleaseModule(release.getReleaseId());
                } catch (Exception e) {
                    log.error("[初始化变量数据失败] 发布任务:{}, 发布工程:{}, 异常:{}", release.getReleaseId(), release.getEngineerName(), e.getMessage());
                    deployService.updateReleaseStatus(release.getReleaseId(), 7);
                    releaseJobStatus = false;
                }

                // 获取数据异常就不继续了
                if (releaseJobStatus) {
                    try {
                        switch (release.getReleaseJobStatus()) {
                            // 待构建，需要工具去构建的任务
                            case 1:
                                // 获取这个发布的所有的阶段信息
                                ArrayList<ReleaseStagesLists> processLists = deployService
                                        .getProcessLists(release.getReleaseId());

                                // 把进度都保存好,用于数据更新
                                Map<String, Long> stages = new HashMap<>();
                                for (ReleaseStagesLists releaseStagesLists : processLists) {
                                    stages.put(releaseStagesLists.getStageName(), releaseStagesLists.getStageId());
                                }

                                // 连接并且创建项目
                                RefV1Api refV1Api = new RefV1Api(
                                        "http://" + JenkinsInfo.getClusterConfigAddress(),
                                        JenkinsInfo.getClusterConfigUsername(),
                                        JenkinsInfo.getClusterConfigPassword());

                                // 判断下任务是否已经提交过，并且构建了，最坏的结果就是再构建一遍
                                if (redisCommonUtils.hasKeys(JenkinsBuild)) {
                                    // 获取结果
                                    ArrayList<Map<String, Object>> results = refV1Api
                                            .JobDetials(release.getEngineerName(),
                                                    Long.parseLong(redisCommonUtils.get(JenkinsBuild).toString()));

                                    // 更新发布结果
                                    deployService.updateReleaseConsoleLog(
                                            release.getReleaseId(),
                                            refV1Api.JobConsoleLog(release.getEngineerName()));
                                    for (Map<String, Object> result : results) {
                                        String JobStageName = result.get("name").toString();
                                        int status = 0;
                                        switch (result.get("status").toString()) {
                                            case "IN_PROGRESS":
                                                // 处理中
                                                status = 1;
                                                break;

                                            case "FAILED":
                                                // 异常更新
                                                deployService.updateReleaseStageStatus(
                                                        release.getReleaseId(), stages.get(JobStageName), 3);
                                                throw new Exception("工程构建失败,主任务退出!");

                                            case "SUCCESS":
                                                // 正常更新
                                                status = 2;
                                                break;
                                        }
                                        deployService.updateReleaseStageStatus(
                                                release.getReleaseId(), stages.get(JobStageName), status);
                                    }

                                    // 只剩下发布就证明前面的流程走完了
                                    if (deployService.checkReleaseLast(release.getReleaseId())) {
                                        // 判断下发布的策略是什么，如果是非谨慎策略，是直接型策略，则直接开始发布任务
                                        if (release.getRulesType() != 0) {
                                            // 更新发布任务为发布中
                                            deployService.updateReleaseStatus(release.getReleaseId(), 3);
                                            if (releaseModule != null) {
                                                this.deploymentJobs(
                                                        release, ClusterAPIInfo,
                                                        true, releaseModule.getReleaseModule(),
                                                        DockerInfo);
                                            } else {
                                                this.deploymentJobs(
                                                        release, ClusterAPIInfo,
                                                        false, null,
                                                        DockerInfo);
                                            }
                                        } else {
                                            // 否则更新为待发布，等待用户手动操作
                                            deployService.updateReleaseStatus(release.getReleaseId(), 2);
                                        }
                                    }
                                } else {
                                    // 执行任务进行jenkins任务提交
                                    // 判断有没有这个工程
                                    if (!refV1Api.hashJob(release.getEngineerName())) {
                                        // 创建项目
                                        refV1Api.createJob(release.getEngineerName());
                                    } else {
                                        // 更新项目xml
                                        refV1Api.updateJob(release.getEngineerName());
                                    }

                                    // 构建项目&传参数
                                    Map<String, String> paratems = new HashMap<>();
                                    paratems.put("GITLAB_ADDR", release.getEngineerGiturl());
                                    paratems.put("GIT_BRANCH", release.getReleaseJobBranch());
                                    paratems.put("JOB_NAME", release.getEngineerName());
                                    paratems.put("JOB_KEYS", DigestUtils.md5DigestAsHex(release.getEngineerName().getBytes()));
                                    paratems.put("SONAR_TOKEN", SonarInfo.getClusterConfigToken());
                                    paratems.put("IMAGE_NAME", release.getEngineerName());
                                    // 模块化参数
                                    if (releaseModule != null) {
                                        paratems.put("MODULE_NAME", releaseModule.getReleaseModule());
                                    }
                                    paratems.put("IMAGE_VERSION", release.getReleaseVersion());
                                    paratems.put("REGISTRY_ADDR", DockerInfo.getClusterConfigAddress());
                                    paratems.put("STORE_AUTH", "k8sStore");
                                    if (DockerfileURL != null) {
                                        paratems.put("DOCKER_FILE_URL", DockerfileURL);
                                    }

                                    // 配置ConfigMap，service信息
                                    KubConfigYaml kubConfigYaml;
                                    if (releaseModule != null) {
                                        kubConfigYaml = clusterService.GetKubConfigMapService(release.getReleaseEngineerId(), release.getReleaseJobNamespace(), releaseModule.getReleaseModule());
                                    } else {
                                        kubConfigYaml = clusterService.GetKubConfigMapService(release.getReleaseEngineerId(), release.getReleaseJobNamespace(), null);
                                    }

                                    // 配置nginx配置
                                    if (kubConfigYaml.getEngineerNginxInfo() != null) {
                                        paratems.put("NGINX_CONF", kubConfigYaml.getEngineerNginxInfo());
                                    }

                                    paratems.put("IMAGE_NAMESPACE", release.getReleaseJobNamespace());
                                    // 构建镜像
                                    Long jobId = refV1Api.buildJob(release.getEngineerName(), paratems);
                                    // 拼装任务探针CI部分
                                    this.redisCommonUtils.noExpireSset(JenkinsBuild, jobId);
                                }
                                break;

                            // 待发布，需要工具去执行发布任务CD
                            case 3:
                                // 判断是否是模块化的
                                if (releaseModule != null) {
                                    this.deploymentJobs(release, ClusterAPIInfo, true, releaseModule.getReleaseModule(), DockerInfo);
                                } else {
                                    this.deploymentJobs(release, ClusterAPIInfo, false, null, DockerInfo);
                                }
                                break;

                            // 待回滚，需要工具去执行回滚任务
                            case 5:
                                // 设置发布的版本是回滚的版本，其它信息不变
                                release.setReleaseVersion(release.getReleaseJobRollback());
                                if (releaseModule != null) {
                                    this.deploymentJobs(release, ClusterAPIInfo, true, releaseModule.getReleaseModule(), DockerInfo);
                                } else {
                                    this.deploymentJobs(release, ClusterAPIInfo, false, null, DockerInfo);
                                }
                                break;
                        }
                    } catch (Exception e) {
                        log.error("[任务处理过程异常] 发布任务ID:{}, 发布工程:{}, 异常:{}", release.getReleaseId(), release.getEngineerName(), e.getMessage());
                        deployService.updateReleaseStatus(release.getReleaseId(), 7);
                    }
                }

                this.redisCommonUtils.getRedisTemplate().delete(TaskLock);
            }
        } catch (Exception e) {
            log.error("ReleaseScheduler.Jobs:" + e.getMessage());
        }
    }

    /**
     * 判断风险得分
     *
     * @param score
     * @param sourceScore
     * @return
     * @throws Exception
     */
    public Float calculateScore(Integer score, Double sourceScore) throws Exception {
        // 先判断风险
        // 致命风险超过50个，1
        // 致命风险超过30个，小于50个，0.8
        // 致命风险超过10个，小于30个，0.5
        // 致命风险超过0个，小于10个，0.3
        // 致命风险超过0个，0
        try {
            if (score >= 50) {
                sourceScore = sourceScore + 1;
            } else if (score > 30) {
                sourceScore = sourceScore + 0.8;
            } else if (score > 10) {
                sourceScore = sourceScore + 0.5;
            } else if (score > 0) {
                sourceScore = sourceScore + 0.3;
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return sourceScore.floatValue();
    }

    /**
     * 发布到集群
     *
     * @param release
     * @param ClusterAPIInfo
     * @throws Exception
     */
    @Retryable(value = Exception.class, maxAttempts = 5)
    public void deploymentJobs(ReleaseExecItems release, ServiceDeployWorkConfig ClusterAPIInfo, Boolean ToModule, String ModuleName, ServiceDeployWorkConfig DockerInfo) throws Exception {
        String TaskLock = "k8sDeploy." + DigestUtils.md5DigestAsHex(release.toString().getBytes());
        try {
            V1ConfigMap v1ConfigMap = null;
            V1Service v1Service = null;
            ExtensionsV1beta1Ingress v1Ingress;
            V1Secret v1Secret = null;
            V1HorizontalPodAutoscaler v1HorizontalPodAutoscaler = null;
            V1Deployment v1Deployment;
            KubIntity kubIntity = new KubIntity();
            kubIntity.setApiServer(ClusterAPIInfo.getClusterConfigAddress());
            kubIntity.setToken(ClusterAPIInfo.getClusterConfigToken());
            KuberContor kuberContor = new KuberContor(kubIntity);

            if (!redisCommonUtils.hasKeys(TaskLock)) {
                redisCommonUtils.noExpireSset(TaskLock, 1);
                deployService.updateReleaseStageToolsStatus(release.getReleaseId(), "业务上线发布", 1);
                // 配置ConfigMap，service信息
                KubConfigYaml kubConfigYaml;
                if (ToModule) {
                    kubConfigYaml = clusterService.GetKubConfigMapService(release.getReleaseEngineerId(), release.getReleaseJobNamespace(), ModuleName);
                } else {
                    kubConfigYaml = clusterService.GetKubConfigMapService(release.getReleaseEngineerId(), release.getReleaseJobNamespace(), null);
                }

                if (kubConfigYaml != null) {
                    v1ConfigMap = Yaml.loadAs(kubConfigYaml.getEngineerConfigmap(), V1ConfigMap.class);
                    v1Service = Yaml.loadAs(kubConfigYaml.getEngineerServices(), V1Service.class);
                }

                ArrayList<Integer> ports = new ArrayList<>();
                List<V1ServicePort> v1ServicePorts = v1Service.getSpec().getPorts();

                for (V1ServicePort v1ServicePort : v1ServicePorts) {
                    ports.add(v1ServicePort.getTargetPort().getIntValue());
                }

                // 加载全局配置
                KubBaseConfig kubBaseConfig;
                if (ToModule) {
                    kubBaseConfig = clusterService.GetKubBaseConfig(
                            release.getReleaseEngineerId(),
                            release.getReleaseJobNamespace(), ModuleName);
                } else {
                    kubBaseConfig = clusterService.GetKubBaseConfig(
                            release.getReleaseEngineerId(),
                            release.getReleaseJobNamespace(), null);
                }

                // 配置ingress
                Boolean ingressHttps = false;
                if (kubBaseConfig.getEngineerIngressHttps() == 1) {
                    ingressHttps = true;
                }

                if (ToModule) {
                    v1Ingress = kuberContor.AutoIngressConf(
                            ModuleName, release.getReleaseJobNamespace(),
                            kubBaseConfig.getEngineerIngressHostName(),
                            ModuleName + "-dssl",
                            ingressHttps);
                } else {
                    v1Ingress = kuberContor.AutoIngressConf(
                            release.getEngineerName(), release.getReleaseJobNamespace(),
                            kubBaseConfig.getEngineerIngressHostName(),
                            release.getEngineerName() + "-dssl",
                            ingressHttps);
                }

                // 如果开启https需要证书
                if (ingressHttps) {
                    if (ToModule) {
                        v1Secret = kuberContor.AutoSecretConfKC(
                                ModuleName, release.getReleaseJobNamespace(),
                                kubConfigYaml.getEngineerCertcrtInfo(), kubConfigYaml.getEngineerCertkeyInfo(),
                                ModuleName + "-dssl");
                    } else {
                        v1Secret = kuberContor.AutoSecretConfKC(
                                release.getEngineerName(), release.getReleaseJobNamespace(),
                                kubConfigYaml.getEngineerCertcrtInfo(), kubConfigYaml.getEngineerCertkeyInfo(),
                                release.getEngineerName() + "-dssl");
                    }
                }

                // 如果开启HPA
//            DolphinsHPA dolphinsHPA = new DolphinsHPA();
//            dolphinsHPA.setCpuQuota(kubBaseConfig.getEngineerHpaCpuQuota());
//            dolphinsHPA.setMaxPod(kubBaseConfig.getEngineerHpaMaxPod());
//            dolphinsHPA.setMinPod(kubBaseConfig.getEngineerHpaMaxPod());
//            if (ToModule){
//                v1HorizontalPodAutoscaler = kubernetes.AutoHPAconf(ModuleName, release.getReleaseJobNamespace(), dolphinsHPA);
//            }else {
//                v1HorizontalPodAutoscaler = kubernetes.AutoHPAconf(release.getEngineerName(), release.getReleaseJobNamespace(), dolphinsHPA);
//            }

                // 发布配置
                DolphinsDeployment dolphinsDeployment = new DolphinsDeployment();
                dolphinsDeployment.setMaxPod(kubBaseConfig.getEngineerDeploymentMaxPod());
                dolphinsDeployment.setMinPod(kubBaseConfig.getEngineerHpaMinPod());
                dolphinsDeployment.setLimitCpu(kubBaseConfig.getEngineerDeploymentLimitCpu());
                dolphinsDeployment.setLimitMemory(kubBaseConfig.getEngineerDeploymentLimitMemory());
                dolphinsDeployment.setReqCpu(kubBaseConfig.getEngineerDeploymentReqCpu());
                dolphinsDeployment.setReqMemory(kubBaseConfig.getEngineerDeploymentReqMemory());
                dolphinsDeployment.setNameSpace(release.getReleaseJobNamespace());
                dolphinsDeployment.setReady(kubBaseConfig.getEngineerReady());

                if (ToModule) {
                    dolphinsDeployment.setServiceImageAddr(
                            DockerInfo.getClusterConfigAddress() + "/"
                                    + release.getReleaseJobNamespace()
                                    + '/' + ModuleName
                                    + ':' + release.getReleaseVersion());
                    dolphinsDeployment.setServiceName(ModuleName);
                } else {
                    dolphinsDeployment.setServiceImageAddr(
                            DockerInfo.getClusterConfigAddress() + "/"
                                    + release.getReleaseJobNamespace() + '/'
                                    + release.getEngineerName() + ':' + release.getReleaseVersion());
                    dolphinsDeployment.setServiceName(release.getEngineerName());
                }

                dolphinsDeployment.setPorts(ports);

                // 拉取镜像专用
                V1Secret v1Secret1;
                if (ToModule) {
                    v1Secret1 = kuberContor.AutoPullImageConf(
                            ModuleName, release.getReleaseJobNamespace(),
                            DockerInfo.getClusterConfigUsername(),
                            DockerInfo.getClusterConfigPassword(),
                            DockerInfo.getClusterConfigAddress());
                } else {
                    v1Secret1 = kuberContor.AutoPullImageConf(
                            release.getEngineerName(), release.getReleaseJobNamespace(),
                            DockerInfo.getClusterConfigUsername(),
                            DockerInfo.getClusterConfigPassword(),
                            DockerInfo.getClusterConfigAddress());
                }

                if (ToModule) {
                    v1Deployment = kuberContor.AutoDeploymentConf(
                            ModuleName, release.getReleaseJobNamespace(),
                            dolphinsDeployment, v1ConfigMap, v1Service);
                } else {
                    v1Deployment = kuberContor.AutoDeploymentConf(
                            release.getEngineerName(), release.getReleaseJobNamespace(),
                            dolphinsDeployment, v1ConfigMap, v1Service);
                }

                kuberContor.initSimpleService(
                        release.getReleaseJobNamespace(), v1ConfigMap,
                        v1Service, v1Ingress,
                        v1HorizontalPodAutoscaler, v1Deployment, v1Secret, v1Secret1
                );
            } else {
                String jobName = "";
                if (ToModule) {
                    jobName = ModuleName;
                } else {
                    jobName = release.getEngineerName();
                }
                // 检测发布结果
                if (kuberContor.getNameSpaceServiceAlive(jobName, release.getReleaseJobNamespace(), release.getReleaseVersion())) {
                    // 如果发布成功就更新
                    deployService.updateReleaseStageToolsStatus(release.getReleaseId(), "业务上线发布", 2);
                    // 如果是回滚，就更新为6
                    if (release.getReleaseJobStatus() == 5) {
                        deployService.updateReleaseStatus(release.getReleaseId(), 6);
                    } else {
                        deployService.updateReleaseStatus(release.getReleaseId(), 4);
                    }
                    this.redisCommonUtils.getRedisTemplate().delete(TaskLock);
                }
            }
        } catch (Exception e) {
            deployService.updateReleaseStatus(release.getReleaseId(), 7);
            this.redisCommonUtils.getRedisTemplate().delete(TaskLock);
            throw new Exception("deploymentJobs:" + e.getMessage());
        }
    }
}
