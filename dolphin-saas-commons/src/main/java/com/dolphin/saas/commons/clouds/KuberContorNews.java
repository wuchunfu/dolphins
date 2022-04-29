package com.dolphin.saas.commons.clouds;

import com.dolphin.saas.commons.clouds.tencent.DomainServPlugin;
import com.dolphin.saas.commons.clouds.tencent.entity.KuberContorKv;
import com.dolphin.saas.commons.clouds.tencent.feature.GitLabServ;
import com.dolphin.saas.commons.clouds.tencent.feature.SonarServ;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Yaml;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.StringReader;
import java.util.*;

@Slf4j
public class KuberContorNews {
    private final KuberContorKv kuberContorKv;
    private final String LastDomain;
    private final Map<String, Object> paramets;
    private final String FilePath;

    /**
     * 基础配置，自动配置
     */
    public KuberContorNews(KuberContorKv kuberContorKv) throws Exception {
        try {
            this.paramets = kuberContorKv.getParamets();
            this.kuberContorKv = kuberContorKv;
            File folder = new File("/yaml");
            if (!folder.exists()) {
                this.FilePath = "yaml";
            } else {
                this.FilePath = "/yaml";
            }
            this.LastDomain = this.paramets.get("cid").toString() + "." + this.paramets.get("masterDomain").toString();

            ApiClient client = ClientBuilder.kubeconfig(
                    KubeConfig.loadKubeConfig(
                            new StringReader(
                                    this.paramets.get("apiServiceToken").toString()
                            )
                    )
            ).setVerifyingSsl(false).build();
            Configuration.setDefaultApiClient(client);
        } catch (Exception e) {
            log.error("[KuberContor]基础认证加载失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 判断是否能够正常访问
     * @return
     * @throws Exception
     */
    public Boolean checkNameSpace() throws Exception {
        try {
            CoreV1Api apiInstance = new CoreV1Api();
            V1NamespaceList v1NamespaceList = apiInstance.listNamespace(
                    null, null,
                    null, null,
                    null, null,
                    null, null,
                    null, null);

            return v1NamespaceList.getItems().size() > 0;
        }catch (Exception e){
            return false;
        }
    }

    public KuberContorNews initNameSpace() throws Exception {
        // 初始化namespace
        CoreV1Api apiInstance = new CoreV1Api();
        List<String> nameSpace = Arrays.asList("devops");
        List<String> aliveNameSpace = new ArrayList<>();
        try {
            // 先获取所有的namespace
            V1NamespaceList v1NamespaceList = apiInstance.listNamespace(
                    null, null,
                    null, null,
                    null, null,
                    null, null,
                    null, null);

            if (v1NamespaceList.getItems().size() > 0) {
                for (V1Namespace namespace : v1NamespaceList.getItems()) {
                    aliveNameSpace.add(namespace.getMetadata().getName());
                }
            }

            for (String s : nameSpace) {
                // 没有才创建
                if (!aliveNameSpace.contains(s)) {
                    V1Namespace v1Namespace = new V1Namespace();
                    v1Namespace.setApiVersion("v1");
                    v1Namespace.setKind("Namespace");
                    V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
                    v1ObjectMeta.setName(s);
                    Map<String, String> Lab = new HashMap<>();
                    Lab.put("name", s);
                    v1ObjectMeta.setLabels(Lab);
                    v1Namespace.setMetadata(v1ObjectMeta);
                    try {
                        apiInstance.createNamespace(v1Namespace, null, null, null);
                    } catch (ApiException e) {
                        throw new ApiException("初始化NameSpace请求失败:" + e.getResponseBody());
                    }
                }
            }
            return this;
        } catch (Exception e) {
            log.error("[部署流水线][基础服务]初始化NameSpace失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 部署服务
     *
     * @return
     * @throws Exception
     */
    public KuberContorNews deploymentService() throws Exception {
        try {
            String cid = this.paramets.get("cid").toString();
            if (this.getNameSpaceJob("create-cluster-job-" + cid, "devops", "alive")) {
                return this;
            }
            BatchV1Api batchV1Api = new BatchV1Api();
            // 归属云获取
            Integer clusterCloud = Integer.parseInt(this.paramets.get("clusterCloud").toString());

            // 购买的模式（小型企业1、中型企业2、大型企业3）
            Integer clusterPayMode = Integer.parseInt(this.paramets.get("clusterPayMode").toString());

            // 基础服务的部署
            V1Job deployJob = Yaml.loadAs(new File(this.FilePath + "/v2/deploy.yaml"), V1Job.class);

            // 设置job名称
            deployJob.getMetadata().setName("create-cluster-job-" + cid);
            deployJob.getSpec().getTemplate().getMetadata().setName("create-cluster-job-" + cid);
            deployJob.getSpec().getTemplate().getSpec().getContainers().get(0).setName("create-cluster-job-" + cid);

            List<V1EnvVar> v1EnvVars = deployJob.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
            if (clusterCloud == 1) {
                v1EnvVars.get(0).setValue("ALIYUN");
            } else {
                v1EnvVars.get(0).setValue("TENCENT");
            }

            switch (clusterPayMode) {
                case 1:
                    v1EnvVars.get(1).setValue("small");
                    break;

                case 2:
                    v1EnvVars.get(1).setValue("medium");
                    break;

                case 3:
                    v1EnvVars.get(1).setValue("big");
                    break;
            }
            v1EnvVars.get(2).setValue(this.paramets.get("apiServiceUrl").toString());
            v1EnvVars.get(3).setValue(this.paramets.get("apiServiceToken").toString());

            // 只有腾讯云需要
            if (clusterCloud == 2) {
                v1EnvVars.get(5).setValue(this.paramets.get("pgroupId").toString());
                v1EnvVars.get(6).setValue(this.paramets.get("subNetId").toString());
            } else {
                v1EnvVars.get(6).setValue("");
            }

            v1EnvVars.get(7).setValue(this.paramets.get("vpcId").toString());
            v1EnvVars.get(8).setValue(this.paramets.get("zone").toString());
            v1EnvVars.get(9).setValue(this.paramets.get("region").toString());
            v1EnvVars.get(10).setValue(this.paramets.get("global_password").toString());
            v1EnvVars.get(11).setValue(this.paramets.get("global_password").toString());
            v1EnvVars.get(12).setValue(this.paramets.get("global_password").toString());
            v1EnvVars.get(13).setValue(this.paramets.get("global_password").toString());

            String LastDomain = this.paramets.get("cid").toString() + "." + this.paramets.get("masterDomain").toString();

            v1EnvVars.get(14).setValue("git-" + LastDomain);
            v1EnvVars.get(15).setValue("grafana-" + LastDomain);
            v1EnvVars.get(16).setValue("hfish-" + LastDomain);
            v1EnvVars.get(17).setValue("jumpserver-" + LastDomain);
            v1EnvVars.get(18).setValue("metersphere-" + LastDomain);
            v1EnvVars.get(19).setValue("skywalking-" + LastDomain);
            v1EnvVars.get(20).setValue("sonar-" + LastDomain);
            v1EnvVars.get(21).setValue("yapi-" + LastDomain);
            v1EnvVars.get(22).setValue("nexus-" + LastDomain);
            v1EnvVars.get(23).setValue("docker-" + LastDomain);
            v1EnvVars.get(24).setValue("jenkins-" + LastDomain);
            v1EnvVars.get(25).setValue("sentry-" + LastDomain);
            v1EnvVars.get(26).setValue("zipkin-" + LastDomain);
            v1EnvVars.get(27).setValue("es-" + LastDomain);
            v1EnvVars.get(28).setValue(cid);
            v1EnvVars.get(29).setValue(this.paramets.get("clusterInstanceId").toString());
            v1EnvVars.get(30).setValue("docker-" + LastDomain);
            v1EnvVars.get(31).setValue("admin");
            v1EnvVars.get(32).setValue("admin123");
            deployJob.getSpec().getTemplate().getSpec().getContainers().get(0).setEnv(v1EnvVars);
            try {
                batchV1Api.createNamespacedJob("devops", deployJob, null, null, null);
            } catch (ApiException e) {
                throw new Exception(e.getResponseBody());
            }
        } catch (Exception e) {
            throw new Exception("[deploymentService]" + e.getMessage());
        }
        return this;
    }

    /**
     * 配置pipeline
     *
     * @return
     * @throws Exception
     */
    public KuberContorNews configurePipeline() throws Exception {
        try {
            // 相互都有探针，只要jenkins可以了，其它都可以了
            if (!this.getNameSpaceService("dolphin-jenkins-service", "devops")) {
                return this;
            }

            String LastDomain = this.paramets.get("cid").toString() + "." + this.paramets.get("masterDomain").toString();

            /*
             * Sonar的处理部分
             */
            // 创建sonar的token
            String SonarToken;

            try {
                SonarToken = new SonarServ(
                        "sonar-" + LastDomain,
                        "admin", this.paramets.get("global_password").toString()).createToken();
            } catch (Exception e) {
                log.warn("[服务部署][流水线配置]配置Sonar初始化不成功，等待下次再试: {}", e.getMessage());
                return this;
            }

            // 设置SONAR的TOken信息
            this.paramets.put("sonar_token", SonarToken);

            /*
             * 配置gitlab
             */
            GitLabServ gitLabServ;
            // 定义需要处理的分组
            Map<String, String> standbyGroup = new HashMap<>();
            standbyGroup.put("devops", "运维业务");
            standbyGroup.put("backend", "后端业务");
            standbyGroup.put("crontab", "定时任务业务");
            standbyGroup.put("arch", "技术架构");
            standbyGroup.put("frontend", "前端业务");

            try {
                gitLabServ = new GitLabServ(
                        "http://git-" + LastDomain,
                        "root",
                        this.paramets.get("global_password").toString());
            } catch (Exception e) {
                log.warn("[服务部署][流水线配置]配置GitLab初始化不成功，等待下次再试: {}", e.getMessage());
                return this;
            }

            // 判断是否已经有了用户了
            ArrayList<String> usersList = gitLabServ.getUsersList();

            if (!usersList.contains("dolphins")) {
                // 创建管理员
                gitLabServ.createUser("dolphins",
                        this.paramets.get("global_password").toString(),
                        "auto@aidolphins.com");
            }

            // 判断是否已经创建过了，如果创建过了就不创建了
            Map<String, Integer> groupNameList = gitLabServ.getGroupNameList();

            // 创建分组
            Map<String, Object> gitGroup = new HashMap<>();

            for (String groupName : standbyGroup.keySet()) {
                Map<String, Integer> groupInfo = new HashMap<>();
                if (!groupNameList.containsKey(groupName)) {
                    groupInfo = gitLabServ.createGroup(groupName, standbyGroup.get(groupName));
                } else {
                    groupInfo.put(groupName, groupNameList.get(groupName));
                }
                gitGroup.put(groupName, groupInfo);
            }
            // 设置Git分组信息
            this.paramets.put("gitGroup", gitGroup);

            /*
             * nexus3的初始化
             */
            try {
                new NexusContor(
                        "nexus-" + LastDomain,
                        "admin",
                        "admin123", false)
                        .cleanRepositories()
                        .addStore()
                        .addHosts()
                        .addAliyunProxy()
                        .addDefaultProxy()
                        .addSpringSnapshotsProxy()
                        .addSprintMilestonesProxy()
                        .addSnapshotsProxy()
                        .addGroup()
                        .addDocker();
            } catch (Exception e) {
                log.warn("[服务部署][流水线配置]配置Nexus3初始化不成功，等待下次再试: {}", e.getMessage());
                return this;
            }

        } catch (Exception e) {
            log.error("[服务部署][配置Pipeline]失败:{}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 配置JENKINS
     *
     * @return
     * @throws Exception
     */
    public KuberContorNews configJenkins() throws Exception {
        try {
            if (!this.paramets.containsKey("sonar_token")) {
                return this;
            }

            String cid = this.paramets.get("cid").toString();
            if (this.getNameSpaceJob("config-cluster-job-" + cid, "devops", "alive")) {
                return this;
            }
            BatchV1Api batchV1Api = new BatchV1Api();
            String LastDomain = cid + "." + this.paramets.get("masterDomain").toString();
            // 处理配置
            V1Job configFile = Yaml.loadAs(new File(this.FilePath + "/v2/config.yaml"), V1Job.class);

            // 设置job名称
            configFile.getMetadata().setName("config-cluster-job-" + cid);
            configFile.getSpec().getTemplate().getMetadata().setName("config-cluster-job-" + cid);
            configFile.getSpec().getTemplate().getSpec().getContainers().get(0).setName("config-cluster-job-" + cid);

            List<V1EnvVar> v1EnvVars = configFile.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
            v1EnvVars.get(0).setValue("http://jenkins-" + LastDomain);
            v1EnvVars.get(2).setValue(this.paramets.get("global_password").toString());
            v1EnvVars.get(3).setValue(this.paramets.get("sonar_token").toString());
            v1EnvVars.get(4).setValue(this.paramets.get("global_password").toString());
            v1EnvVars.get(5).setValue("root");
            v1EnvVars.get(6).setValue("admin123");
            v1EnvVars.get(7).setValue("admin");
            configFile.getSpec().getTemplate().getSpec().getContainers().get(0).setEnv(v1EnvVars);
            try {
                batchV1Api.createNamespacedJob("devops", configFile, null, null, null);
            } catch (ApiException e) {
                throw new Exception(e.getResponseBody());
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return this;
    }

    public Map<String, Object> doubleCheck() throws Exception {
        Map<String, Object> results = new HashMap<>();
        String cid = this.paramets.get("cid").toString();

        if (!this.paramets.containsKey("gitGroup") || !this.paramets.containsKey("sonar_token")) {
            results.put("state", "PROCESS");
        } else if (!this.getNameSpaceJob("create-cluster-job-" + cid, "devops", "status")) {
            results.put("state", "PROCESS");
        } else if (!this.getNameSpaceJob("config-cluster-job-" + cid, "devops", "status")) {
            results.put("state", "PROCESS");
        }

        if (results.containsKey("state")) {
            return results;
        }

        // 清除jobs
        this.deleteNameSpaceJob();

        results.put("state", "SUCCESS");
        results.put("gitGroup", this.paramets.get("gitGroup"));
        results.put("sonar_token", this.paramets.get("sonar_token"));
        return results;
    }

    /**
     * 初始化域名
     *
     * @return
     * @throws Exception
     */
    public KuberContorNews initDomain() throws Exception {
        try {
            /*
             * 设置域名和证书
             */
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    // 如果ingress的服务还没部署好就不执行
                    if (!this.getNameSpaceService("ingress-nginx", "kube-system")) {
                        return this;
                    }
                    // 设置ingressIP
                    this.paramets.put("ingressIp",
                            this.getNameSpaceServiceIP("nginx-ingress-lb", "kube-system"));
                    break;

                case 2:
                    // 如果ingress的服务还没部署好就不执行
                    if (!this.getNameSpaceService("nginx-ingress", "devops")) {
                        return this;
                    }
                    // 设置ingressIP
                    String clusterIp = this.getNameSpaceServiceIP("nginx-ingress-controller", "devops");
                    if (clusterIp == null) {
                        return this;
                    }
                    this.paramets.put("ingressIp", clusterIp);
                    break;
            }

            // 创建域名
            new DomainServPlugin(
                    this.paramets.get("masterAccessKeyId").toString(),
                    this.paramets.get("masterAccessKeySecret").toString())
                    .setVal(this.paramets)
                    .runner();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 删除JOB
     *
     * @throws Exception
     */
    protected void deleteNameSpaceJob() throws Exception {
        try {
            String cid = this.paramets.get("cid").toString();
            BatchV1Api batchV1Api = new BatchV1Api();
            batchV1Api.deleteNamespacedJob("create-cluster-job-" + cid, "devops", null, null, null, null, null, null);
            batchV1Api.deleteNamespacedJob("config-cluster-job-" + cid, "devops", null, null, null, null, null, null);
        } catch (ApiException e) {
            log.warn("deleteNameSpaceJob: " + e.getResponseBody());
        }
    }

    /**
     * 判断job是否完成
     *
     * @param app
     * @param namespace
     * @return
     * @throws Exception
     */
    protected Boolean getNameSpaceJob(String app, String namespace, String type) throws Exception {
        try {
            BatchV1Api batchV1Api = new BatchV1Api();
            V1JobList v1JobList = batchV1Api.listNamespacedJob(namespace, null, null, null, null, null, null, null, null, null, null);
            if (type.equals("alive")) {
                List<String> Jobs = new ArrayList<>();
                for (V1Job item : v1JobList.getItems()) {
                    Jobs.add(item.getMetadata().getName());
                }
                return Jobs.contains(app);
            }
            if (type.equals("status")) {
                for (V1Job item : v1JobList.getItems()) {
                    if (item.getMetadata().getName().equals(app)) {
                        return item.getStatus().getSucceeded() == 1;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 获取pod的状态
     *
     * @return
     * @throws Exception
     */
    protected Boolean getNameSpaceService(String app, String namespace) throws Exception {
        Map<String, String> results = new HashMap<>();
        try {
            CoreV1Api apiInstance = new CoreV1Api();
            V1PodList list = apiInstance.listNamespacedPod(
                    namespace, null, null, null, null, null, null, null, null, null, null);
            // 遍历list，循环判断pod的状态，并完成pod的调度
            for (V1Pod item : list.getItems()) {
                results.put(item.getMetadata().getLabels().get("app"), item.getStatus().getPhase());
            }
        } catch (Exception e) {
            log.error("[服务部署][获取pod状态]失败:{}", e.getMessage());
            throw new Exception(e.getMessage());
        }

        if (results.containsKey(app)) {
            return results.get(app).equals("Running");
        }

        return false;
    }

    /**
     * 根据Service Name 获取对应的Ip
     *
     * @param app
     * @param namespace
     * @return
     */
    protected String getNameSpaceServiceIP(String app, String namespace) {
        String IP = null;
        try {
            CoreV1Api apiInstance = new CoreV1Api();
            V1Service v1Service = apiInstance.readNamespacedService(app, namespace, null, null, null);
            List<V1LoadBalancerIngress> Ingress = v1Service.getStatus().getLoadBalancer().getIngress();

            if (Ingress.size() > 0) {
                IP = v1Service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
            }
        } catch (Exception e) {
            log.error("[服务部署][获取IP]应用: {}, 命名空间: {}, 失败: {}", app, namespace, e.getMessage());
        }
        return IP;
    }
}
