package com.dolphin.saas.commons.clouds;

import com.dolphin.saas.commons.clouds.tencent.DomainServPlugin;
import com.dolphin.saas.commons.clouds.tencent.entity.DolphinsDeployment;
import com.dolphin.saas.commons.clouds.tencent.entity.DolphinsHPA;
import com.dolphin.saas.commons.clouds.tencent.entity.KubIntity;
import com.dolphin.saas.commons.clouds.tencent.feature.GitLabServ;
import com.dolphin.saas.commons.clouds.tencent.feature.SonarServ;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.*;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Yaml;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

/**
 * 配合settings.xml用
 * <servers>
 * <server>
 * <id>snapshots</id>
 * <username>admin</username>
 * <password>admin123</password>
 * </server>
 *
 * <server>
 * <id>releases</id>
 * <username>admin</username>
 * <password>admin123</password>
 * </server>
 * </servers>
 *
 *
 * <mirrors>
 * <mirror>
 * <id>snapshots</id>
 * <name>group-maven</name>
 * <url>https://nexus.171.aidolphins.com/repository/group-maven/</url>
 * <mirrorOf>*</mirrorOf>
 * </mirror>
 * </mirrors>
 */


@Slf4j
public class KuberContor {

    private final KubIntity kubIntity;

    private Map<String, Object> paramets;
    private final String FilePath;

    /**
     * 基础配置，自动配置
     */
    public KuberContor(KubIntity kubIntity) throws Exception {
        try {
            File folder = new File("/yaml");
            if (!folder.exists()) {
                this.FilePath = "yaml";
            } else {
                this.FilePath = "/yaml";
            }
            this.kubIntity = kubIntity;
            ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new StringReader(kubIntity.getToken()))).setVerifyingSsl(false).build();
            Configuration.setDefaultApiClient(client);
        } catch (Exception e) {
            log.error("[部署流水线][基础服务]基础认证加载失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }

    }

    /**
     * 设置变量
     *
     * @param paramets
     * @return
     */
    public KuberContor setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    /**
     * 初始化Ingress controller
     *
     * @return
     * @throws Exception
     */
    public KuberContor initIngressController() throws Exception {
        // 只有腾讯云才需要装
        if (Integer.parseInt(this.paramets.get("clusterCloud").toString()) != 2) {
            return this;
        }

        // 如果已经有了就不用再创建了
        if (this.aliveNameSpaceService("nginx-ingress", "nginx-ingress")) {
            return this;
        }

        try {
            /*
             * 判断有没有ingress controller对应的命名空间
             * 没有就意味着没有创建过，有就直接返回
             */
            CoreV1Api apiInstance = new CoreV1Api();
            V1NamespaceList v1NamespaceList = apiInstance.listNamespace(
                    null, null,
                    null, null,
                    null, null,
                    null, null,
                    null, null);

            if (v1NamespaceList.getItems().size() > 0) {
                for (V1Namespace namespace : v1NamespaceList.getItems()) {
                    if (namespace.getMetadata().getName().equals("nginx-ingress")) {
                        return this;
                    }
                }
            }

            // 创建namespace
            V1Namespace v1Namespace = new V1Namespace();
            v1Namespace.setApiVersion("v1");
            v1Namespace.setKind("Namespace");
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName("nginx-ingress");
            Map<String, String> Lab = new HashMap<>();
            Lab.put("name", "nginx-ingress");
            v1ObjectMeta.setLabels(Lab);
            v1Namespace.setMetadata(v1ObjectMeta);
            try {
                apiInstance.createNamespace(v1Namespace, null, null, null);
            } catch (ApiException e) {
                throw new ApiException("创建namespace失败: " + e.getResponseBody());
            }

            /*
             * 创建ingress controller
             */
            List<Object> ingressController = Yaml.loadAll(new File(this.FilePath + "/ingressController/all.yaml"));
            for (Object obj : ingressController) {
                RbacAuthorizationV1Api rbacAuthorizationV1Api = new RbacAuthorizationV1Api();

                // 配置configMap
                if (obj.getClass().equals(V1ConfigMap.class)) {
                    V1ConfigMap v1ConfigMap = (V1ConfigMap) obj;
                    apiInstance.createNamespacedConfigMap(
                            "nginx-ingress", v1ConfigMap,
                            null, null, null);
                }

                // 配置V1beta1PodDisruptionBudget
                if (obj.getClass().equals(V1beta1PodDisruptionBudget.class)) {
                    V1beta1PodDisruptionBudget v1beta1PodDisruptionBudget = (V1beta1PodDisruptionBudget) obj;
                    new PolicyV1beta1Api().createNamespacedPodDisruptionBudget(
                            "nginx-ingress", v1beta1PodDisruptionBudget,
                            null, null, null);
                }

                // 配置V1ServiceAccount
                if (obj.getClass().equals(V1ServiceAccount.class)) {
                    V1ServiceAccount v1ServiceAccount = (V1ServiceAccount) obj;
                    apiInstance.createNamespacedServiceAccount("nginx-ingress", v1ServiceAccount, null, null, null);
                }

                // 配置V1ClusterRole
                if (obj.getClass().equals(V1ClusterRole.class)) {
                    V1ClusterRole v1ClusterRole = (V1ClusterRole) obj;
                    rbacAuthorizationV1Api.createClusterRole(v1ClusterRole, null
                            , null, null);
                }

                // 配置V1ClusterRoleBinding
                if (obj.getClass().equals(V1ClusterRoleBinding.class)) {
                    V1ClusterRoleBinding v1ClusterRoleBinding = (V1ClusterRoleBinding) obj;
                    rbacAuthorizationV1Api.
                            createClusterRoleBinding(v1ClusterRoleBinding, null, null
                                    , null);

                }

                // 配置V1Role
                if (obj.getClass().equals(V1Role.class)) {
                    V1Role v1Role = (V1Role) obj;
                    rbacAuthorizationV1Api.createNamespacedRole("nginx-ingress", v1Role, null, null, null);
                }

                // 配置V1RoleBinding
                if (obj.getClass().equals(V1RoleBinding.class)) {
                    V1RoleBinding v1RoleBinding = (V1RoleBinding) obj;
                    rbacAuthorizationV1Api.createNamespacedRoleBinding("nginx-ingress", v1RoleBinding, null, null, null);
                }

                // 配置V1Service
                if (obj.getClass().equals(V1Service.class)) {
                    V1Service v1Service = (V1Service) obj;
                    apiInstance.createNamespacedService(
                            "nginx-ingress", v1Service,
                            null, null,
                            null);
                }

                // 配置V1Deployment
                if (obj.getClass().equals(V1Deployment.class)) {
                    V1Deployment v1Deployment = (V1Deployment) obj;
                    new AppsV1Api()
                            .createNamespacedDeployment(
                                    "nginx-ingress", v1Deployment,
                                    null, null,
                                    null);
                }
            }
        } catch (Exception e) {
            log.error("[部署流水线][基础服务]初始化IngressController失败: {}", e.getMessage());
            throw new Exception("初始化IngressController失败: -10000");
        }
        return this;
    }

    public KuberContor initDomainSecrt() throws Exception {
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
                    if (!this.getNameSpaceService("nginx-ingress", "nginx-ingress")) {
                        return this;
                    }
                    // 设置ingressIP
                    String clusterIp = this.getNameSpaceServiceIP("nginx-ingress-controller", "nginx-ingress");
                    if (clusterIp == null){
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

//
//            // 获取证书
//            Map<String, Object> secretMap = new SslServPlugin(
//                    this.paramets.get("masterAccessKeyId").toString(),
//                    this.paramets.get("masterAccessKeySecret").toString())
//                    .setVal(this.paramets)
//                    .runner()
//                    .refval();
//
//            if (secretMap.containsKey("nexus") && secretMap.containsKey("docker")) {
//                // 保存证书
//                this.paramets.put("secretMap", secretMap);
//                // 创建域名
//                new DomainServPlugin(
//                        this.paramets.get("masterAccessKeyId").toString(),
//                        this.paramets.get("masterAccessKeySecret").toString())
//                        .setVal(this.paramets)
//                        .runner();
//            } else {
//                return this;
//            }
        } catch (Exception e) {
            log.error("[部署流水线][基础服务]初始化域名和证书失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 初始化namespace
     *
     * @return
     * @throws Exception
     */
    public KuberContor initNameSpace() throws Exception {
        // 初始化namespace
        CoreV1Api apiInstance = new CoreV1Api();
        List<String> nameSpace = Arrays.asList(
                "test", "dev",
                "demos", "online", "devops");
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
     * 设置基础服务通用的mysql
     *
     * @return
     * @throws Exception
     */
    public KuberContor initMysql() throws Exception {
        try {
            // 如果已经有了就不用再创建了
            if (this.aliveNameSpaceService("dolphin-mysql-service", "devops")) {
                return this;
            }

            CoreV1Api apiInstance = new CoreV1Api();

            V1PersistentVolume v1PersistentVolume = null;
            V1PersistentVolumeClaim v1PersistentVolumeClaim = null;
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    v1PersistentVolume = this.AutoPersistentVolumeAliyun(
                            "dolphin-mysql-service", "devops",
                            160, this.kubIntity.getNfsHost(),
                            this.paramets.get("mysql_nfs_path").toString());

                    v1PersistentVolumeClaim = this.AutoPersistentVolumeClaimAliyun(
                            "dolphin-mysql-service",
                            "devops",
                            160
                    );
                    break;

                case 2:
                    v1PersistentVolume = Yaml.loadAs(
                            new File(this.FilePath + "/mysql/PersistentVolume.yaml"),
                            V1PersistentVolume.class);

                    // 修改pcv信息
                    v1PersistentVolume.getSpec().getCsi().setVolumeHandle(this.kubIntity.getNfsId());
                    Map<String, String> volumeAttr = new HashMap<>();
                    volumeAttr.put("host", this.kubIntity.getNfsHost());
                    volumeAttr.put("path", this.paramets.get("mysql_nfs_path").toString());
                    v1PersistentVolume.getSpec().getCsi().setVolumeAttributes(volumeAttr);
                    v1PersistentVolumeClaim = Yaml.loadAs(
                            new File(this.FilePath + "/mysql/PersistentVolumeClaim.yaml"),
                            V1PersistentVolumeClaim.class);
                    break;
            }

            /*
             * 设置service
             */
            ArrayList<V1ServicePort> v1ServicePorts = new ArrayList<>();
            V1ServicePort v1ServicePort = new V1ServicePort();
            v1ServicePort.setPort(3306);
            v1ServicePort.setTargetPort(new IntOrString(3306));
            v1ServicePort.setProtocol("TCP");
            v1ServicePort.setName("mysql");
            v1ServicePorts.add(v1ServicePort);

            // 先用烧钱模式先跑通整体逻辑
            V1Service v1Service = this.AutoServiceConf(
                    "dolphin-mysql-service",
                    "devops", v1ServicePorts,
                    true);

            /*
             * 设置Deployment
             */
            V1Deployment v1Deployment = Yaml.loadAs(
                    new File(this.FilePath + "/mysql/deployment.yaml"),
                    V1Deployment.class);

            // 修改deployment里设置的默认密码、访问域名
            String replaceInfo = v1Deployment
                    .getSpec().getTemplate()
                    .getSpec().getContainers()
                    .get(0).getEnv()
                    .get(0).getValue()
                    .replace("<PASSWORD>", this.paramets.get("mysql_password").toString());
            v1Deployment.getSpec()
                    .getTemplate().getSpec()
                    .getContainers().get(0)
                    .getEnv().get(0).setValue(replaceInfo);

            apiInstance.createPersistentVolume(
                    v1PersistentVolume, null,
                    null, null);

            apiInstance.createNamespacedPersistentVolumeClaim(
                    "devops", v1PersistentVolumeClaim,
                    null, null,
                    null);
            apiInstance.createNamespacedService(
                    "devops", v1Service,
                    null, null,
                    null);
            new AppsV1Api()
                    .createNamespacedDeployment(
                            "devops", v1Deployment,
                            null, null,
                            null);
            return this;
        } catch (Exception e) {
            log.error("[部署流水线][基础服务]MySQL部署失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 自动配置阿里云PV
     *
     * @param app       服务
     * @param nameSpace 命名空间
     * @param HandSize  NFS的尺寸
     * @param NasServer NAS的地址
     * @param NasPath   NAS、NFS的挂载路径
     * @return
     * @throws Exception
     */
    public V1PersistentVolume AutoPersistentVolumeAliyun(String app, String nameSpace, Integer HandSize, String NasServer, String NasPath) throws Exception {
        V1PersistentVolume v1PersistentVolume;
        try {
            v1PersistentVolume = Yaml.loadAs(
                    new File(this.FilePath + "/aliyun/PersistentVolume.yaml"),
                    V1PersistentVolume.class);
            v1PersistentVolume.getMetadata().setName(app);
            v1PersistentVolume.getMetadata().setNamespace(nameSpace);
            Map<String, String> labels = new HashMap<>();
            labels.put("alicloud-pvname", app);
            v1PersistentVolume.getMetadata().setLabels(labels);
            Map<String, Quantity> capacity = new HashMap<>();
            capacity.put("storage", new Quantity(HandSize + "Gi"));
            v1PersistentVolume.getSpec().setCapacity(capacity);
            v1PersistentVolume.getSpec().getCsi().setVolumeHandle(app);
            Map<String, String> volumeAttr = new HashMap<>();
            volumeAttr.put("server", NasServer);
            volumeAttr.put("path", NasPath);
            volumeAttr.put("vers", "4.0");
            v1PersistentVolume.getSpec().getCsi().setVolumeAttributes(volumeAttr);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return v1PersistentVolume;
    }

    /**
     * 自动配置阿里云PVC
     *
     * @param app       服务
     * @param nameSpace 命名空间
     * @param HandSize  NFS的尺寸
     * @return
     * @throws Exception
     */
    public V1PersistentVolumeClaim AutoPersistentVolumeClaimAliyun(String app, String nameSpace, Integer HandSize) throws Exception {
        V1PersistentVolumeClaim v1PersistentVolumeClaim;
        try {
            v1PersistentVolumeClaim = Yaml.loadAs(
                    new File(this.FilePath + "/aliyun/PersistentVolumeClaim.yaml"),
                    V1PersistentVolumeClaim.class);
            v1PersistentVolumeClaim.getMetadata().setName(app);
            v1PersistentVolumeClaim.getMetadata().setNamespace(nameSpace);

            Map<String, Quantity> requests = new HashMap<>();
            requests.put("storage", new Quantity(HandSize + "Gi"));
            v1PersistentVolumeClaim.getSpec().getResources().setRequests(requests);
            Map<String, String> matchLabels = new HashMap<>();
            matchLabels.put("alicloud-pvname", app);
            v1PersistentVolumeClaim.getSpec().getSelector().setMatchLabels(matchLabels);
            v1PersistentVolumeClaim.getSpec().setVolumeName(app);
            v1PersistentVolumeClaim.getStatus().setCapacity(requests);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return v1PersistentVolumeClaim;
    }

    /**
     * 部署Gitlab项目，这个对外提供访问，需要有域名
     *
     * @throws Exception
     */
    public KuberContor initGitLab() throws Exception {
        try {
            // 如果已经有了就不用再创建了
            if (this.aliveNameSpaceService("dolphin-gitlab-service", "devops")) {
                return this;
            }
            CoreV1Api apiInstance = new CoreV1Api();
            ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();
            NetworkingV1Api networkingV1Api = new NetworkingV1Api();

            ExtensionsV1beta1Ingress v1Ingress = null;
            V1Ingress v1Ingress1 = null;
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    // ingress业务
                    v1Ingress1 = this.AutoIngressAliyunConf(
                            "dolphin-gitlab-service",
                            "devops",
                            this.paramets.get("gitlab_domain").toString(),
                            null,
                            false);
                    try {
                        networkingV1Api.createNamespacedIngress(
                                "devops", v1Ingress1, null, null, null);
                    }catch (ApiException e){
                        throw new Exception("GitLab部署Ingress失败, 错误信息: "+e.getResponseBody());
                    }
                    break;
                case 2:
                    // ingress业务
                    v1Ingress = this.AutoIngressConf(
                            "dolphin-gitlab-service",
                            "devops",
                            this.paramets.get("gitlab_domain").toString(),
                            null,
                            false);
                    try {
                        extensionsV1beta1Api.createNamespacedIngress(
                                "devops", v1Ingress, null, null, null);
                    }catch (ApiException e){
                        throw new Exception("GitLab部署Ingress失败, 错误信息: "+e.getResponseBody());
                    }
                    break;
            }

            V1PersistentVolume v1PersistentVolume = null;
            V1PersistentVolumeClaim v1PersistentVolumeClaim = null;
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    v1PersistentVolume = this.AutoPersistentVolumeAliyun(
                            "dolphin-gitlab-service", "devops",
                            160, this.kubIntity.getNfsHost(),
                            this.paramets.get("gitlab_nfs_path").toString());

                    v1PersistentVolumeClaim = this.AutoPersistentVolumeClaimAliyun(
                            "dolphin-gitlab-service",
                            "devops",
                            160
                    );
                    break;

                case 2:
                    v1PersistentVolume = Yaml
                            .loadAs(new File(this.FilePath + "/gitlab/PersistentVolume.yaml"),
                                    V1PersistentVolume.class);
                    // 修改pcv信息
                    v1PersistentVolume.getSpec().getCsi().setVolumeHandle(this.kubIntity.getNfsId());
                    Map<String, String> volumeAttr = new HashMap<>();
                    volumeAttr.put("host", this.kubIntity.getNfsHost());
                    volumeAttr.put("path", this.paramets.get("gitlab_nfs_path").toString());
                    v1PersistentVolume.getSpec().getCsi().setVolumeAttributes(volumeAttr);
                    v1PersistentVolumeClaim = Yaml
                            .loadAs(new File(this.FilePath + "/gitlab/PersistentVolumeClaim.yaml"),
                                    V1PersistentVolumeClaim.class);
                    break;
            }

            /*
             * 设置service
             */
            ArrayList<V1ServicePort> v1ServicePorts = new ArrayList<>();
            V1ServicePort v1ServicePort = new V1ServicePort();
            v1ServicePort.setPort(80);
            v1ServicePort.setTargetPort(new IntOrString(80));
            v1ServicePort.setProtocol("TCP");
            v1ServicePorts.add(v1ServicePort);
            V1Service v1Service = this
                    .AutoServiceConf(
                            "dolphin-gitlab-service",
                            "devops",
                            v1ServicePorts, false);

            /*
             * 设置deployment
             */
            V1Deployment v1Deployment = Yaml
                    .loadAs(new File(this.FilePath + "/gitlab/deployment.yaml"), V1Deployment.class);

            // 修改deployment里设置的默认密码、访问域名
            String replaceInfo = v1Deployment.getSpec()
                    .getTemplate()
                    .getSpec()
                    .getContainers()
                    .get(0).getEnv()
                    .get(1).getValue().replace("<PassWord>", this.paramets.get("gitlab_password").toString())
                    .replace("<Domain>", this.paramets.get("gitlab_domain").toString());
            v1Deployment.getSpec()
                    .getTemplate()
                    .getSpec()
                    .getContainers()
                    .get(0)
                    .getEnv()
                    .get(1)
                    .setValue(replaceInfo);
            try {
                apiInstance.createPersistentVolume(
                        v1PersistentVolume, null, null, null);
                apiInstance.createNamespacedPersistentVolumeClaim(
                        "devops", v1PersistentVolumeClaim, null, null, null);
                apiInstance.createNamespacedService(
                        "devops", v1Service, null, null, null);
                new AppsV1Api().createNamespacedDeployment(
                        "devops", v1Deployment, null, null, null);
            }catch (ApiException e){
                throw new Exception("部署GitLab失败, 异常信息:" + e.getResponseBody());
            }

            return this;
        } catch (Exception e) {
            log.error("[部署流水线][基础服务]GitLab部署失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 部署sonar项目
     *
     * @return
     * @throws Exception
     */
    public KuberContor initSonar() throws Exception {
        try {
            // 如果已经有了就不用再创建了
            if (this.aliveNameSpaceService("dolphin-sonar-service", "devops")) {
                return this;
            }
            // 如果MYSQL的服务还没部署好就不执行
            if (!this.getNameSpaceService("dolphin-mysql-service", "devops")) {
                return this;
            }
            // 获取MYSQL外网的IP
            String outSideIp = this.getNameSpaceServiceIP("dolphin-mysql-service", "devops");

            // 获取不到IP就返回，免访问测试
            if (outSideIp == null) {
                return this;
            }

            // 创建数据库，失败就返回，下次再来
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");//添加一个驱动类
                String SQL = "create database sonar";
                String databaseUrl = "jdbc:mysql://" + outSideIp + ":3306?useUnicode=true&chararcterEncoding=utf8";//设置mysql数据库的地址
                Connection connection = DriverManager.getConnection(
                        databaseUrl, "root", this.paramets.get("mysql_password").toString());
                connection.createStatement().execute(SQL);
            } catch (Exception e) {
                log.error("[服务部署][基础服务]Sonar配置MYSQL未成功,稍后再试!信息: {}", e.getMessage());
                return this;
            }

            CoreV1Api apiInstance = new CoreV1Api();
            ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();
            NetworkingV1Api networkingV1Api = new NetworkingV1Api();

            /*
             * 设置service
             */
            ArrayList<V1ServicePort> v1ServicePorts = new ArrayList<>();
            V1ServicePort v1ServicePort = new V1ServicePort();
            v1ServicePort.setPort(80);
            v1ServicePort.setTargetPort(new IntOrString(9000));
            v1ServicePort.setProtocol("TCP");
            v1ServicePort.setName("http");
            v1ServicePorts.add(v1ServicePort);
            V1Service v1Service = this.AutoServiceConf("dolphin-sonar-service", "devops", v1ServicePorts, false);

            ExtensionsV1beta1Ingress v1Ingress = null;
            V1Ingress v1Ingress1 = null;
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    // ingress业务
                    v1Ingress1 = this.AutoIngressAliyunConf(
                            "dolphin-sonar-service",
                            "devops",
                            this.paramets.get("sonar_domain").toString(),
                            null,
                            false);
                    networkingV1Api.createNamespacedIngress(
                            "devops", v1Ingress1, null, null, null);
                    break;
                case 2:
                    // ingress业务
                    v1Ingress = this.AutoIngressConf(
                            "dolphin-sonar-service",
                            "devops",
                            this.paramets.get("sonar_domain").toString(),
                            null,
                            false);
                    extensionsV1beta1Api.createNamespacedIngress(
                            "devops", v1Ingress, null, null, null);
                    break;
            }

            V1PersistentVolume v1PersistentVolume = null;
            V1PersistentVolumeClaim v1PersistentVolumeClaim = null;
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    v1PersistentVolume = this.AutoPersistentVolumeAliyun(
                            "dolphin-sonar-service", "devops",
                            160, this.kubIntity.getNfsHost(),
                            this.paramets.get("sonar_nfs_path").toString());

                    v1PersistentVolumeClaim = this.AutoPersistentVolumeClaimAliyun(
                            "dolphin-sonar-service",
                            "devops",
                            160
                    );
                    break;

                case 2:
                    v1PersistentVolumeClaim = Yaml.loadAs(new File(this.FilePath + "/sonar/PersistentVolumeClaim.yaml"), V1PersistentVolumeClaim.class);
                    v1PersistentVolume = Yaml.loadAs(new File(this.FilePath + "/sonar/PersistentVolume.yaml"), V1PersistentVolume.class);

                    // 修改pvc信息
                    v1PersistentVolume.getSpec().getCsi().setVolumeHandle(this.kubIntity.getNfsId());
                    Map<String, String> volumeAttr = new HashMap<>();
                    volumeAttr.put("host", this.kubIntity.getNfsHost());
                    volumeAttr.put("path", this.paramets.get("sonar_nfs_path").toString());
                    v1PersistentVolume.getSpec().getCsi().setVolumeAttributes(volumeAttr);
                    break;
            }

            try {
                apiInstance.createPersistentVolume(
                        v1PersistentVolume, null, null, null);
                apiInstance.createNamespacedPersistentVolumeClaim(
                        "devops", v1PersistentVolumeClaim, null, null, null);
            }catch (ApiException e){
                throw new Exception("initSonarError: "+ e.getResponseBody());
            }


            V1Deployment v1Deployment = Yaml.loadAs(new File(this.FilePath + "/sonar/deployment.yaml"), V1Deployment.class);
            List<V1EnvVar> v1EnvVars = v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
            v1EnvVars.get(0).setValue(v1EnvVars.get(0).getValue().replace("<HOSTS>", "dolphin-mysql-service"));
            v1EnvVars.get(1).setValue(v1EnvVars.get(1).getValue().replace("<PASSWORD>", this.paramets.get("mysql_password").toString()));
            v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setEnv(v1EnvVars);

            //增加存活探测
            V1Probe readinessProbe = new V1Probe();
            // 尝试次数
            readinessProbe.setFailureThreshold(60);
            // 请求探针
            V1HTTPGetAction httpGet = new V1HTTPGetAction();
            httpGet.setPath("/");
            httpGet.setPort(new IntOrString(9000));
            httpGet.setScheme("HTTP");
            readinessProbe.setHttpGet(httpGet);
            // 初始化的时间预计(45秒初始化完这个服务)
            readinessProbe.setInitialDelaySeconds(15);
            // 执行检查间隔
            readinessProbe.setPeriodSeconds(5);
            // 最小连续成功数
            readinessProbe.setSuccessThreshold(1);
            // 超时等待时间
            readinessProbe.setTimeoutSeconds(1);
            v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setReadinessProbe(readinessProbe);

            apiInstance.createNamespacedService(
                    "devops", v1Service,
                    null, null,
                    null);

            new AppsV1Api().createNamespacedDeployment(
                    "devops", v1Deployment,
                    null, null,
                    null);
            return this;
        } catch (Exception e) {
            log.error("[部署流水线][基础服务]Sonar部署失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 部署私有仓库
     *
     * @return
     * @throws Exception
     */
    public KuberContor initNexus() throws Exception {
        try {
            // 如果已经有了就不用再创建了
            if (this.aliveNameSpaceService("dolphin-nexus-service", "devops")) {
                return this;
            }
//            // 判断证书是否存在
//            if (!this.paramets.containsKey("secretMap")) {
//                return this;
//            }

            CoreV1Api apiInstance = new CoreV1Api();
            ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();
            NetworkingV1Api networkingV1Api = new NetworkingV1Api();

            /*
             * 设置service
             */
            ArrayList<V1ServicePort> v1ServicePorts = new ArrayList<>();
            V1ServicePort v1ServicePort = new V1ServicePort();
            v1ServicePort.setPort(80);
            v1ServicePort.setTargetPort(new IntOrString(8081));
            v1ServicePort.setProtocol("TCP");
            v1ServicePort.setName("http");
            v1ServicePorts.add(v1ServicePort);
            V1ServicePort v1ServicePort2 = new V1ServicePort();
            v1ServicePort2.setPort(5000);
            v1ServicePort2.setTargetPort(new IntOrString(5000));
            v1ServicePort2.setProtocol("TCP");
            v1ServicePort2.setName("docker");
            v1ServicePorts.add(v1ServicePort2);

            V1Service v1Service = this.AutoServiceConf(
                    "dolphin-nexus-service", "devops", v1ServicePorts, false);

            /*
             * 创建证书 不用证书了，因为可以用
             */
//            Map<String, Object> secretMap = (Map<String, Object>) this.paramets.get("secretMap");
//            Map<String, Object> nexusMap = (Map<String, Object>) secretMap.get("nexus");
//            Map<String, Object> dockerMap = (Map<String, Object>) secretMap.get("docker");
//            V1Secret v1NexusSecret = AutoSecretConfAliyunKC("",
//                    "devops",
//                    nexusMap.get("crt").toString(),
//                    nexusMap.get("key").toString(),
//                    "dolphin-nexus-ssl-secret"
//            );
//            V1Secret v1DockerSecret = AutoSecretConfAliyunKC("",
//                    "devops",
//                    dockerMap.get("crt").toString(),
//                    dockerMap.get("key").toString(),
//                    "dolphin-docker-ssl-secret"
//            );
//            apiInstance.createNamespacedSecret("devops", v1NexusSecret, null, null, null);
//            apiInstance.createNamespacedSecret("devops", v1DockerSecret, null, null, null);

            /*
             * ingress业务
             */
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    V1Ingress extensionsV1beta1Ingress = new V1Ingress();
                    extensionsV1beta1Ingress.setApiVersion("networking.k8s.io/v1");
                    extensionsV1beta1Ingress.setKind("Ingress");
                    V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
                    v1ObjectMeta.setName("dolphin-nexus-service");
                    v1ObjectMeta.setNamespace("devops");

                    // 头部定义
//                    Map<String, String> Annotations = new HashMap<>();
//                    Annotations.put("kubernetes.io/ingress.class", "nginx");
//                    Annotations.put("kubernetes.io/ingress.rule-mix", "true");
//                    Annotations.put("nginx.ingress.kubernetes.io/ssl-redirect", "true");
//                    Annotations.put("nginx.ingress.kubernetes.io/use-regex", "true");
//                    Annotations.put("ingress.kubernetes.io/proxy-body-size", "200m");
//                    Annotations.put("kubernetes.io/tls-acme", "true");

//                    v1ObjectMeta.setAnnotations(Annotations);
                    extensionsV1beta1Ingress.setMetadata(v1ObjectMeta);

                    // 定义spec
                    V1IngressSpec v1IngressSpec = new V1IngressSpec();
                    v1IngressSpec.setIngressClassName("nginx");

                    /**
                     * 仓库主站
                     */

                    // 定义spec
                    V1IngressRule v1IngressRule = new V1IngressRule();
                    List<V1IngressRule> v1IngressRuleList = new ArrayList<>();
                    v1IngressRule.setHost(this.paramets.get("nexus_domain").toString());
                    V1HTTPIngressRuleValue http = new V1HTTPIngressRuleValue();
                    List<V1HTTPIngressPath> paths = new ArrayList<>();
                    V1HTTPIngressPath v1HTTPIngressPath = new V1HTTPIngressPath();
                    V1IngressBackend backend = new V1IngressBackend();
                    V1IngressServiceBackend service = new V1IngressServiceBackend();
                    service.setName("dolphin-nexus-service");
                    V1ServiceBackendPort port = new V1ServiceBackendPort();
                    port.setNumber(80);
                    service.setPort(port);
                    backend.setService(service);
                    v1HTTPIngressPath.setBackend(backend);
                    v1HTTPIngressPath.setPath("/");
                    v1HTTPIngressPath.setPathType("ImplementationSpecific");
                    paths.add(v1HTTPIngressPath);
                    http.setPaths(paths);
                    v1IngressRule.setHttp(http);
                    v1IngressRuleList.add(v1IngressRule);

                    /**
                     * docker
                     */

                    V1IngressRule v1IngressRule1 = new V1IngressRule();
                    v1IngressRule1.setHost(this.paramets.get("docker_domain").toString());
                    V1HTTPIngressRuleValue http1 = new V1HTTPIngressRuleValue();
                    List<V1HTTPIngressPath> paths1 = new ArrayList<>();
                    V1HTTPIngressPath v1HTTPIngressPath1 = new V1HTTPIngressPath();
                    V1IngressBackend backend1 = new V1IngressBackend();
                    V1IngressServiceBackend service1 = new V1IngressServiceBackend();
                    service1.setName("dolphin-nexus-service");
                    V1ServiceBackendPort port1 = new V1ServiceBackendPort();
                    port1.setNumber(5000);
                    service1.setPort(port1);
                    backend1.setService(service1);
                    v1HTTPIngressPath1.setBackend(backend1);
                    v1HTTPIngressPath1.setPath("/");
                    v1HTTPIngressPath1.setPathType("ImplementationSpecific");
                    paths1.add(v1HTTPIngressPath1);
                    http1.setPaths(paths1);
                    v1IngressRule1.setHttp(http);
                    v1IngressRuleList.add(v1IngressRule1);

                    v1IngressSpec.setRules(v1IngressRuleList);



                    /**
                     * 配置HTTPS
                     *
                     */

                    // 如果有https，则需要加证书
//                    V1IngressTLS v1IngressTLS = new V1IngressTLS();
//                    v1IngressTLS.setSecretName("dolphin-nexus-ssl-secret");
//                    List<String> host = new ArrayList<>();
//                    host.add(this.paramets.get("nexus_domain").toString());
//                    v1IngressTLS.setHosts(host);
//
//                    V1IngressTLS v1IngressTLS1 = new V1IngressTLS();
//                    v1IngressTLS1.setSecretName("dolphin-docker-ssl-secret");
//                    List<String> host1 = new ArrayList<>();
//                    host1.add(this.paramets.get("docker_domain").toString());
//                    v1IngressTLS1.setHosts(host1);
//
//                    List<V1IngressTLS> v1IngressTLSList = new ArrayList<>();
//                    v1IngressTLSList.add(v1IngressTLS);
//                    v1IngressTLSList.add(v1IngressTLS1);
//                    v1IngressSpec.setTls(v1IngressTLSList);
                    extensionsV1beta1Ingress.setSpec(v1IngressSpec);
                    try {
                        networkingV1Api.createNamespacedIngress("devops", extensionsV1beta1Ingress, null, null, null);
                    }catch (ApiException e){
                        throw new Exception(e.getResponseBody());
                    }
                    break;
                case 2:
                    ExtensionsV1beta1Ingress v1Ingress = Yaml.loadAs(new File(this.FilePath + "/nexus/Ingress.yaml"), ExtensionsV1beta1Ingress.class);

//                    List<ExtensionsV1beta1IngressTLS> tls = new ArrayList<>();

//                    List<String> hosts = new ArrayList<>();
//                    hosts.add(this.paramets.get("nexus_domain").toString());

//                    List<String> hosts2 = new ArrayList<>();
//                    hosts2.add(this.paramets.get("docker_domain").toString());

//                    ExtensionsV1beta1IngressTLS extensionsV1beta1IngressTLS = new ExtensionsV1beta1IngressTLS();
//                    extensionsV1beta1IngressTLS.setHosts(hosts);
//                    extensionsV1beta1IngressTLS.setSecretName("dolphin-nexus-ssl-secret");
//                    tls.add(extensionsV1beta1IngressTLS);
//
//                    ExtensionsV1beta1IngressTLS extensionsV1beta1IngressTLS2 = new ExtensionsV1beta1IngressTLS();
//                    extensionsV1beta1IngressTLS2.setHosts(hosts2);
//                    extensionsV1beta1IngressTLS2.setSecretName("dolphin-docker-ssl-secret");
//                    tls.add(extensionsV1beta1IngressTLS2);

                    v1Ingress.getSpec().setTls(null);

                    v1Ingress.getSpec().getRules().get(0).setHost(this.paramets.get("nexus_domain").toString());
                    v1Ingress.getSpec().getRules().get(1).setHost(this.paramets.get("docker_domain").toString());

                    extensionsV1beta1Api.createNamespacedIngress(
                            "devops", v1Ingress, null, null, null);
                    break;
            }


            V1PersistentVolume v1PersistentVolume = null;
            V1PersistentVolumeClaim v1PersistentVolumeClaim = null;
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    v1PersistentVolume = this.AutoPersistentVolumeAliyun(
                            "dolphin-nexus-service", "devops",
                            160, this.kubIntity.getNfsHost(),
                            this.paramets.get("nexus_nfs_path").toString());

                    v1PersistentVolumeClaim = this.AutoPersistentVolumeClaimAliyun(
                            "dolphin-nexus-service",
                            "devops",
                            160
                    );
                    break;

                case 2:
                    v1PersistentVolumeClaim = Yaml.loadAs(new File(this.FilePath + "/nexus/PersistentVolumeClaim.yaml"), V1PersistentVolumeClaim.class);
                    v1PersistentVolume = Yaml.loadAs(new File(this.FilePath + "/nexus/PersistentVolume.yaml"), V1PersistentVolume.class);

                    // 修改pvc信息
                    v1PersistentVolume.getSpec().getCsi().setVolumeHandle(this.kubIntity.getNfsId());
                    Map<String, String> volumeAttr = new HashMap<>();
                    volumeAttr.put("host", this.kubIntity.getNfsHost());
                    volumeAttr.put("path", this.paramets.get("nexus_nfs_path").toString());
                    v1PersistentVolume.getSpec().getCsi().setVolumeAttributes(volumeAttr);
                    break;
            }

            V1Deployment v1Deployment = Yaml.loadAs(new File(this.FilePath + "/nexus/deployment.yaml"), V1Deployment.class);

            apiInstance.createPersistentVolume(v1PersistentVolume, null, null, null);
            apiInstance.createNamespacedPersistentVolumeClaim("devops", v1PersistentVolumeClaim, null, null, null);
            apiInstance.createNamespacedService(
                    "devops", v1Service,
                    null, null,
                    null);

            new AppsV1Api().createNamespacedDeployment(
                    "devops", v1Deployment,
                    null, null,
                    null);
            return this;
        } catch (Exception e) {
            log.error("[部署流水线][基础服务]Nexus部署失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 部署镜像漏洞扫描
     *
     * @return
     * @throws Exception
     */
    public KuberContor initTrivy() throws Exception {
        return this;
    }

    /**
     * 初始化JENKINS服务
     *
     * @return
     * @throws Exception
     */
    public KuberContor initJenkins() throws Exception {
        try {
            // 如果已经有了就不用再创建了
            if (this.aliveNameSpaceService("dolphin-jenkins-service", "devops")) {
                return this;
            }

            // 预先检查是否已经有所需的所有值
            List<String> checkLists = Arrays.asList(
                    "jenkins_password", "sonar_token",
                    "apiServiceUrl", "jenkins_domain",
                    "tcr_username", "tcr_token",
                    "gitlab_password", "jenkins_nfs_path");
            for (String checkItems : checkLists) {
                if (!this.paramets.containsKey(checkItems)) {
                    return this;
                }
            }

            List<String> serivces = Arrays.asList(
                    "dolphin-gitlab-service", "dolphin-mysql-service",
                    "dolphin-sonar-service", "dolphin-nexus-service");
            for (String serviceName : serivces) {
                // 只要有一个服务没有部署好，就不部署JENKINS
                if (!this.getNameSpaceService(serviceName, "devops")) {
                    return this;
                }
            }
            CoreV1Api apiInstance = new CoreV1Api();

            V1PersistentVolume v1PersistentVolume = null;
            V1PersistentVolumeClaim v1PersistentVolumeClaim = null;
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    v1PersistentVolume = this.AutoPersistentVolumeAliyun(
                            "dolphin-jenkins-service", "devops",
                            160, this.kubIntity.getNfsHost(),
                            this.paramets.get("jenkins_nfs_path").toString());

                    v1PersistentVolumeClaim = this.AutoPersistentVolumeClaimAliyun(
                            "dolphin-jenkins-service",
                            "devops",
                            160
                    );
                    break;

                case 2:
                    v1PersistentVolume = Yaml.loadAs(new File(this.FilePath + "/jenkins/PersistentVolume.yaml"), V1PersistentVolume.class);
                    v1PersistentVolume.getSpec().getCsi().setVolumeHandle(this.kubIntity.getNfsId());
                    Map<String, String> volumeAttr = new HashMap<>();
                    volumeAttr.put("host", this.kubIntity.getNfsHost());
                    volumeAttr.put("path", this.paramets.get("jenkins_nfs_path").toString());
                    v1PersistentVolume.getSpec().getCsi().setVolumeAttributes(volumeAttr);
                    v1PersistentVolumeClaim = Yaml.loadAs(new File(this.FilePath + "/jenkins/PersistentVolumeClaim.yaml"), V1PersistentVolumeClaim.class);
                    break;
            }

            ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();
            NetworkingV1Api networkingV1Api = new NetworkingV1Api();

            ExtensionsV1beta1Ingress v1Ingress = null;
            V1Ingress v1Ingress1 = null;
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    // ingress业务
                    v1Ingress1 = this.AutoIngressAliyunConf(
                            "dolphin-jenkins-service",
                            "devops",
                            this.paramets.get("jenkins_domain").toString(),
                            null,
                            false);
                    networkingV1Api.createNamespacedIngress(
                            "devops", v1Ingress1, null, null, null);
                    break;
                case 2:
                    // ingress业务
                    v1Ingress = this.AutoIngressConf(
                            "dolphin-jenkins-service",
                            "devops",
                            this.paramets.get("jenkins_domain").toString(),
                            null,
                            false);
                    extensionsV1beta1Api.createNamespacedIngress(
                            "devops", v1Ingress, null, null, null);
                    break;
            }

            V1Service v1Service = Yaml.loadAs(new File(this.FilePath + "/jenkins/Service.yaml"), V1Service.class);
            V1Deployment v1Deployment = Yaml.loadAs(new File(this.FilePath + "/jenkins/deployment.yaml"), V1Deployment.class);


            // 初始化
            apiInstance.createPersistentVolume(v1PersistentVolume, null, null, null);
            apiInstance.createNamespacedPersistentVolumeClaim("devops", v1PersistentVolumeClaim, null, null, null);
            apiInstance.createNamespacedService("devops", v1Service, null, null, null);

            // 初始化关键参数
            List<V1EnvVar> v1EnvVars = v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
            v1EnvVars.get(0).setValue(v1EnvVars.get(0).getValue().replace("<PASSWORD>", this.paramets.get("jenkins_password").toString()));
            v1EnvVars.get(1).setValue(v1EnvVars.get(1).getValue().replace("<SONAR_TOKEN>", this.paramets.get("sonar_token").toString()));
            v1EnvVars.get(2).setValue(v1EnvVars.get(2).getValue().replace("<GITLAB_TOKEN>", "无效"));
            v1EnvVars.get(3).setValue(v1EnvVars.get(3).getValue().replace("<TCR_USERNAME>", this.paramets.get("tcr_username").toString()));
            v1EnvVars.get(4).setValue(v1EnvVars.get(4).getValue().replace("<TCR_TOKEN>", this.paramets.get("tcr_token").toString()));
            v1EnvVars.get(5).setValue(v1EnvVars.get(5).getValue().replace("<SONAR_SERVER_URL>", "dolphin-sonar-service"));
            v1EnvVars.get(6).setValue(v1EnvVars.get(6).getValue().replace("<K8S_ADDR>", this.paramets.get("apiServiceUrl").toString()));
            v1EnvVars.get(7).setValue(v1EnvVars.get(7).getValue().replace("<JENKINS_ADDR>", this.paramets.get("jenkins_domain").toString()));
            v1EnvVars.get(8).setValue(v1EnvVars.get(8).getValue().replace("<GITLAB_PASSWORD>", this.paramets.get("gitlab_password").toString()));

            v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setEnv(v1EnvVars);
            try {
                new AppsV1Api().createNamespacedDeployment("devops", v1Deployment, null, null, null);
            }catch (ApiException e){
                throw new Exception(e.getResponseBody());
            }
            return this;
        } catch (Exception e) {
            log.error("[部署流水线][基础服务]Jenkins部署失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 判断服务是否都部署完
     *
     * @return
     * @throws Exception
     */
    public Map<String, Object> serviceAllAlive(Integer level) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            // 判断服务是否已经都部署完
            // 判断是否都能正常访问
            List<String> serivces = Arrays.asList(
                    "dolphin-gitlab-service", "dolphin-mysql-service",
                    "dolphin-sonar-service", "dolphin-nexus-service",
                    "dolphin-jenkins-service");

            if (level == 2){
                serivces.add("dolphin-yapi-service");
                serivces.add("dolphin-hfish-service");
            }

            for (String serviceName : serivces) {
                // 只要有一个服务没有部署好，就不部署JENKINS
                if (!this.getNameSpaceService(serviceName, "devops")) {
                    results.put("state", "PROCESS");
                    return results;
                }
            }
            if (!this.paramets.containsKey("gitGroup")){
                results.put("state", "PROCESS");
                return results;
            }
            results.put("state", "SUCCESS");
            results.put("gitGroup", this.paramets.get("gitGroup"));
            results.put("sonar_token", this.paramets.get("sonar_token"));
            return results;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 设置服务的configmap
     *
     * @param kvData      configmap的数据
     * @param serviceName 服务名
     * @return V1ConfigMap对象
     */
    public V1ConfigMap AutoConfigMapConf(Map<String, String> kvData, String serviceName, String nameSpace) throws Exception {
        V1ConfigMap v1ConfigMap = new V1ConfigMap();
        try {
            // 设置头部
            v1ConfigMap.setApiVersion("v1");
            v1ConfigMap.setKind("ConfigMap");
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(serviceName);
            v1ObjectMeta.setNamespace(nameSpace);
            v1ConfigMap.setMetadata(v1ObjectMeta);
            // 配置数据
            v1ConfigMap.setData(kvData);
        } catch (Exception e) {
            log.error("[服务发布][ConfigMap配置]配置失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return v1ConfigMap;
    }

    /**
     * 设置服务的service
     *
     * @param serviceName    服务名
     * @param v1ServicePorts 开放的端口列表
     * @return V1Service
     */
    public V1Service AutoServiceConf(
            String serviceName, String nameSpace,
            ArrayList<V1ServicePort> v1ServicePorts,
            Boolean serviceType) throws Exception {
        V1Service v1Service = new V1Service();
        try {
            // 构建label
            Map<String, String> labels = new HashMap<>();
            labels.put("app", serviceName);

            // 构建头部
            v1Service.setApiVersion("v1");
            v1Service.setKind("Service");
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(serviceName);
            v1ObjectMeta.setNamespace(nameSpace);
            v1ObjectMeta.setLabels(labels);
            v1Service.setMetadata(v1ObjectMeta);

            // 构建spec
            V1ServiceSpec v1ServiceSpec = new V1ServiceSpec();
            if (!serviceType) {
                // 省钱模式，转发
                v1ServiceSpec.setType("ClusterIP");
            } else {
                // 这个是烧钱模式
                v1ServiceSpec.setType("LoadBalancer");
            }

            v1ServiceSpec.setSelector(labels);
            // 端口
            v1ServiceSpec.setPorts(v1ServicePorts);
            v1Service.setSpec(v1ServiceSpec);
        } catch (Exception e) {
            log.error("[服务发布][Service配置]配置失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return v1Service;
    }

    /**
     * 自动配置ingress，支持http/https
     *
     * @param serviceName 服务名
     * @param hostName    域名地址（解析才生效）
     * @param SecretName  证书名
     * @param httpsMode   是否开启https（True：开,False：不开）
     * @return V1Ingress
     */
    public ExtensionsV1beta1Ingress AutoIngressConf(String serviceName, String nameSpace, String hostName, String SecretName, Boolean httpsMode) throws Exception {
        ExtensionsV1beta1Ingress extensionsV1beta1Ingress = new ExtensionsV1beta1Ingress();
        try {
            extensionsV1beta1Ingress.setApiVersion("extensions/v1beta1");
            extensionsV1beta1Ingress.setKind("Ingress");
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(serviceName);
            v1ObjectMeta.setNamespace(nameSpace);

            // 头部定义
            Map<String, String> Annotations = new HashMap<>();
            Annotations.put("kubernetes.io/ingress.class", "nginx");
            if (httpsMode) {
                Annotations.put("kubernetes.io/ingress.rule-mix", "true");
                Annotations.put("nginx.ingress.kubernetes.io/ssl-redirect", "true");
                Annotations.put("nginx.ingress.kubernetes.io/use-regex", "true");
                Annotations.put("ingress.kubernetes.io/proxy-body-size", "200m");
                Annotations.put("kubernetes.io/tls-acme", "true");
            }

            v1ObjectMeta.setAnnotations(Annotations);
            extensionsV1beta1Ingress.setMetadata(v1ObjectMeta);

            // 定义spec
            ExtensionsV1beta1IngressSpec extensionsV1beta1IngressSpec = new ExtensionsV1beta1IngressSpec();
            ExtensionsV1beta1IngressRule extensionsV1beta1IngressRule = new ExtensionsV1beta1IngressRule();
            extensionsV1beta1IngressRule.setHost(hostName);

            ExtensionsV1beta1HTTPIngressPath extensionsV1beta1HTTPIngressPath = new ExtensionsV1beta1HTTPIngressPath();
            extensionsV1beta1HTTPIngressPath.setPath("/");
            ExtensionsV1beta1IngressBackend extensionsV1beta1IngressBackend = new ExtensionsV1beta1IngressBackend();

            extensionsV1beta1IngressBackend.setServiceName(serviceName);
            extensionsV1beta1IngressBackend.setServicePort(new IntOrString(80));

            extensionsV1beta1HTTPIngressPath.setBackend(extensionsV1beta1IngressBackend);
            ExtensionsV1beta1HTTPIngressRuleValue extensionsV1beta1HTTPIngressRuleValue = new ExtensionsV1beta1HTTPIngressRuleValue();
            List<ExtensionsV1beta1HTTPIngressPath> extensionsV1beta1HTTPIngressPathArrayList = new ArrayList<>();
            extensionsV1beta1HTTPIngressPathArrayList.add(extensionsV1beta1HTTPIngressPath);
            extensionsV1beta1HTTPIngressRuleValue.setPaths(extensionsV1beta1HTTPIngressPathArrayList);
            extensionsV1beta1IngressRule.setHttp(extensionsV1beta1HTTPIngressRuleValue);
            extensionsV1beta1IngressSpec.addRulesItem(extensionsV1beta1IngressRule);

            // 如果有https，则需要加证书
            if (httpsMode) {
                ExtensionsV1beta1IngressTLS extensionsV1beta1IngressTLS = new ExtensionsV1beta1IngressTLS();
                extensionsV1beta1IngressTLS.setSecretName(SecretName);
                List<String> host = new ArrayList<>();
                host.add(hostName);
                extensionsV1beta1IngressTLS.setHosts(host);
                extensionsV1beta1IngressSpec.addTlsItem(extensionsV1beta1IngressTLS);
            }

            // 最终设置
            extensionsV1beta1Ingress.setSpec(extensionsV1beta1IngressSpec);
        } catch (Exception e) {
            log.error("[服务发布][Ingress配置]配置失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return extensionsV1beta1Ingress;
    }

    /**
     * 自动配置ingress，支持http/https
     *
     * @param serviceName 服务名
     * @param hostName    域名地址（解析才生效）
     * @param SecretName  证书名
     * @param httpsMode   是否开启https（True：开,False：不开）
     * @return V1Ingress
     */
    public V1Ingress AutoIngressAliyunConf(String serviceName, String nameSpace, String hostName, String SecretName, Boolean httpsMode) throws Exception {
        V1Ingress extensionsV1beta1Ingress = new V1Ingress();
        try {
            extensionsV1beta1Ingress.setApiVersion("networking.k8s.io/v1");
            extensionsV1beta1Ingress.setKind("Ingress");
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(serviceName);
            v1ObjectMeta.setNamespace(nameSpace);

            // 头部定义
            Map<String, String> Annotations = new HashMap<>();
//            Annotations.put("kubernetes.io/ingress.class", "nginx");
            if (httpsMode) {
                Annotations.put("kubernetes.io/ingress.rule-mix", "true");
                Annotations.put("nginx.ingress.kubernetes.io/ssl-redirect", "true");
                Annotations.put("nginx.ingress.kubernetes.io/use-regex", "true");
                Annotations.put("ingress.kubernetes.io/proxy-body-size", "200m");
                Annotations.put("kubernetes.io/tls-acme", "true");
            }

            v1ObjectMeta.setAnnotations(Annotations);
            extensionsV1beta1Ingress.setMetadata(v1ObjectMeta);

            // 定义spec
            V1IngressSpec v1IngressSpec = new V1IngressSpec();
            v1IngressSpec.setIngressClassName("nginx");
            V1IngressRule v1IngressRule = new V1IngressRule();
            List<V1IngressRule> v1IngressRuleList = new ArrayList<>();
            v1IngressRule.setHost(hostName);
            V1HTTPIngressRuleValue http = new V1HTTPIngressRuleValue();
            List<V1HTTPIngressPath> paths = new ArrayList<>();
            V1HTTPIngressPath v1HTTPIngressPath = new V1HTTPIngressPath();
            V1IngressBackend backend = new V1IngressBackend();
            V1IngressServiceBackend service = new V1IngressServiceBackend();
            service.setName(serviceName);
            V1ServiceBackendPort port = new V1ServiceBackendPort();
            port.setNumber(80);
            service.setPort(port);
            backend.setService(service);
            v1HTTPIngressPath.setBackend(backend);
            v1HTTPIngressPath.setPath("/");
            v1HTTPIngressPath.setPathType("ImplementationSpecific");
            paths.add(v1HTTPIngressPath);
            http.setPaths(paths);
            v1IngressRule.setHttp(http);
            v1IngressRuleList.add(v1IngressRule);
            v1IngressSpec.setRules(v1IngressRuleList);

            // 如果有https，则需要加证书
            if (httpsMode) {
                V1IngressTLS v1IngressTLS = new V1IngressTLS();
                v1IngressTLS.setSecretName(SecretName);
                List<String> host = new ArrayList<>();
                host.add(hostName);
                v1IngressTLS.setHosts(host);
                List<V1IngressTLS> v1IngressTLSList = new ArrayList<>();
                v1IngressTLSList.add(v1IngressTLS);
                v1IngressSpec.setTls(v1IngressTLSList);
            }

            extensionsV1beta1Ingress.setSpec(v1IngressSpec);
        } catch (Exception e) {
            log.error("[服务发布][Ingress配置]配置失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return extensionsV1beta1Ingress;
    }

    /**
     * 设置证书
     *
     * @param serviceName 服务名
     * @param nameSpace   命名空间
     * @return V1Secret
     * @throws Exception
     */
    public V1Secret AutoSecretConf(String serviceName, String nameSpace, String CertificateId) throws Exception {
        V1Secret v1Secret = new V1Secret();
        try {
            v1Secret.setApiVersion("v1");
            Map<String, String> qclouds = new HashMap<>();
            qclouds.put("qcloud_cert_id", CertificateId);
            v1Secret.setStringData(qclouds);
            v1Secret.setKind("Secret");
            v1Secret.setType("Opaque");

            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(serviceName);
            v1ObjectMeta.setNamespace(nameSpace);
            v1Secret.setMetadata(v1ObjectMeta);
        } catch (Exception e) {
            log.error("[服务发布][Secret配置]配置失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return v1Secret;
    }

    /**
     * 设置证书(标准证书)
     *
     * @param app       服务名
     * @param nameSpace 命名空间
     * @return V1Secret
     * @throws Exception
     */
    public V1Secret AutoSecretConfKC(String app, String nameSpace, String CertificateCrt, String CertificateKey, String CertName) throws Exception {
        V1Secret v1Secret = new V1Secret();
        try {
            v1Secret.setApiVersion("v1");
            Map<String, String> qclouds = new HashMap<>();
            qclouds.put("tls.crt", CertificateCrt);
            qclouds.put("tls.key", CertificateKey);
            v1Secret.setStringData(qclouds);
            v1Secret.setKind("Secret");
            v1Secret.setType("kubernetes.io/tls");
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            if (CertName != null) {
                v1ObjectMeta.setName(CertName);
            } else {
                v1ObjectMeta.setName(app);
            }
            v1ObjectMeta.setNamespace(nameSpace);
            Map<String, String> labels = new HashMap<>();
            if (CertName != null) {
                labels.put("qcloud-app", CertName);
            } else {
                labels.put("qcloud-app", app);
            }
            v1ObjectMeta.setLabels(labels);
            v1Secret.setMetadata(v1ObjectMeta);
        } catch (Exception e) {
            log.error("[服务发布][Secret配置]配置失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return v1Secret;
    }

    /**
     * 设置证书(标准证书)
     *
     * @param app       服务名
     * @param nameSpace 命名空间
     * @return V1Secret
     * @throws Exception
     */
    public V1Secret AutoSecretConfAliyunKC(String app, String nameSpace, String CertificateCrt, String CertificateKey, String CertName) throws Exception {
        V1Secret v1Secret = new V1Secret();
        try {
            v1Secret.setApiVersion("v1");
            Map<String, String> qclouds = new HashMap<>();
            qclouds.put("tls.crt", CertificateCrt);
            qclouds.put("tls.key", CertificateKey);
            v1Secret.setStringData(qclouds);
            v1Secret.setKind("Secret");
            v1Secret.setType("IngressTLS");
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            if (CertName != null) {
                v1ObjectMeta.setName(CertName);
            } else {
                v1ObjectMeta.setName(app);
            }
            v1ObjectMeta.setNamespace(nameSpace);
            v1Secret.setMetadata(v1ObjectMeta);
        } catch (Exception e) {
            log.error("[服务发布][Secret配置]配置失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return v1Secret;
    }


    /**
     * 设置访问凭证 拉取镜像
     *
     * @param serviceName
     * @param nameSpace
     * @param userName
     * @param passWord
     * @param storeName
     * @return
     * @throws Exception
     */
    public V1Secret AutoPullImageConf(String serviceName, String nameSpace, String userName, String passWord, String storeName) throws Exception {
        V1Secret v1Secret = new V1Secret();
        try {
            v1Secret.setApiVersion("v1");
            v1Secret.setKind("Secret");
            v1Secret.setType("kubernetes.io/dockercfg");
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(serviceName);
            v1ObjectMeta.setNamespace(nameSpace);
            Map<String, String> labels = new HashMap<>();
            labels.put("qcloud-app", serviceName);
            v1ObjectMeta.setLabels(labels);
            v1Secret.setMetadata(v1ObjectMeta);
            Map<String, String> keys = new HashMap<>();
            keys.put(".dockercfg", this.createToken(storeName, userName, passWord));
            v1Secret.setStringData(keys);
        } catch (Exception e) {
            log.error("AutoPullImageConf:" + e);
            throw new Exception(e.getMessage());
        }
        return v1Secret;
    }

    /**
     * 创建一个拉取镜像访问密钥的token
     *
     * @param UserName
     * @param PassWord
     * @return
     */
    protected String createToken(String store, String UserName, String PassWord) {
        String userpass = UserName + ":" + PassWord;

        return "{\"" + store + "\":{\"username\":\"" + UserName + "\",\"password\":\"" + PassWord + "\",\"auth\":\"" + Base64.getEncoder().encodeToString(userpass.getBytes()) + "\"}}";
    }

    /**
     * 配置Deployment发布
     *
     * @param serviceName        服务名
     * @param nameSpace          命名空间
     * @param dolphinsDeployment deployment必须的配置
     * @return V1Deployment对象
     * @throws Exception 标准异常
     */
    public V1Deployment AutoDeploymentConf(String serviceName, String nameSpace, DolphinsDeployment dolphinsDeployment, V1ConfigMap v1ConfigMap, V1Service v1Service) throws Exception {
        V1Deployment v1Deployment = new V1Deployment();
        try {
            v1Deployment.setApiVersion("apps/v1");
            v1Deployment.setKind("Deployment");

            // 设置头部meta
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setNamespace(nameSpace);
            v1ObjectMeta.setName(serviceName);
            Map<String, String> labels = new HashMap<>();
            labels.put("app", serviceName);
            v1ObjectMeta.setLabels(labels);
            v1Deployment.setMetadata(v1ObjectMeta);

            // 设置spec
            V1DeploymentSpec v1DeploymentSpec = new V1DeploymentSpec();
            v1DeploymentSpec.setRevisionHistoryLimit(dolphinsDeployment.getMinPod());
            // 副本数
            v1DeploymentSpec.setReplicas(dolphinsDeployment.getMaxPod());

            V1DeploymentStrategy v1DeploymentStrategy = new V1DeploymentStrategy();
            v1DeploymentStrategy.setType("RollingUpdate");
            v1DeploymentSpec.setStrategy(v1DeploymentStrategy);
            V1LabelSelector v1LabelSelector = new V1LabelSelector();
            v1LabelSelector.setMatchLabels(labels);
            v1DeploymentSpec.setSelector(v1LabelSelector);

            V1PodTemplateSpec v1PodTemplateSpec = new V1PodTemplateSpec();
            V1ObjectMeta v1ObjectMeta1 = new V1ObjectMeta();
            v1ObjectMeta1.setLabels(labels);
            v1PodTemplateSpec.setMetadata(v1ObjectMeta1);
            V1Container v1Container = new V1Container();

            // 设置一个port地址列表
            List<V1ContainerPort> v1ContainerPortList = new ArrayList<>();
            for (Integer port : dolphinsDeployment.getPorts()) {
                V1ContainerPort v1ContainerPort = new V1ContainerPort();
                v1ContainerPort.setProtocol("TCP");
                v1ContainerPort.setContainerPort(port);
                v1ContainerPortList.add(v1ContainerPort);
            }
            v1Container.setPorts(v1ContainerPortList);
            v1Container.setName(serviceName);
            // 设置镜像地址
            v1Container.setImage(dolphinsDeployment.getServiceImageAddr());
            // 设置env引用
            if (v1ConfigMap != null) {
                V1EnvFromSource v1EnvFromSource = new V1EnvFromSource();
                V1ConfigMapEnvSource v1ConfigMapEnvSource = new V1ConfigMapEnvSource();
                v1ConfigMapEnvSource.setName(serviceName);
                v1EnvFromSource.setConfigMapRef(v1ConfigMapEnvSource);
                List<V1EnvFromSource> v1EnvFromSourceList = new ArrayList<>();
                v1EnvFromSourceList.add(v1EnvFromSource);
                v1Container.setEnvFrom(v1EnvFromSourceList);
            }

            // 配置资源
            Map<String, Quantity> Limits = new HashMap<>();
            Limits.put("cpu", Quantity.fromString(dolphinsDeployment.getLimitCpu().toString()));
            Limits.put("memory", Quantity.fromString(dolphinsDeployment.getLimitMemory().toString() + "Mi"));

            Map<String, Quantity> Requests = new HashMap<>();
            Requests.put("cpu", Quantity.fromString(dolphinsDeployment.getReqCpu().toString()));
            Requests.put("memory", Quantity.fromString(dolphinsDeployment.getReqMemory().toString() + "Mi"));

            V1ResourceRequirements v1ResourceRequirements = new V1ResourceRequirements();
            v1ResourceRequirements.setRequests(Requests);
            v1ResourceRequirements.setLimits(Limits);
            v1Container.setResources(v1ResourceRequirements);
            V1PodSpec v1PodSpec = new V1PodSpec();

            // 创建拉取配置
            V1LocalObjectReference v1LocalObjectReference = new V1LocalObjectReference();
            v1LocalObjectReference.setName(serviceName);
            List<V1LocalObjectReference> v1LocalObjectReferenceList = new ArrayList<>();
            v1LocalObjectReferenceList.add(v1LocalObjectReference);
            v1PodSpec.setImagePullSecrets(v1LocalObjectReferenceList);

            List<V1Container> v1ContainerList = new ArrayList<>();
            v1ContainerList.add(v1Container);
            v1PodSpec.setContainers(v1ContainerList);
            v1PodTemplateSpec.setSpec(v1PodSpec);
            v1DeploymentSpec.setTemplate(v1PodTemplateSpec);
            v1Deployment.setSpec(v1DeploymentSpec);

            // 判断是否开启就绪探针
            if (dolphinsDeployment.getReady() == 1){
                V1Probe readinessProbe = new V1Probe();
                // 尝试次数
                readinessProbe.setFailureThreshold(60);
                // 请求探针
                V1HTTPGetAction httpGet = new V1HTTPGetAction();
                httpGet.setPath("/");
                // 遍历找到80端口
                IntOrString targetPort = null;
                if (v1Service.getSpec().getPorts().size() > 0){
                    for (V1ServicePort portItems: v1Service.getSpec().getPorts()){
                        if (portItems.getPort() == 80){
                            targetPort = portItems.getTargetPort();
                        }
                    }
                }
                httpGet.setPort(targetPort);
                httpGet.setScheme("HTTP");
                readinessProbe.setHttpGet(httpGet);
                // 初始化的时间预计(10秒初始化完这个服务)
                readinessProbe.setInitialDelaySeconds(10);
                // 执行检查间隔
                readinessProbe.setPeriodSeconds(5);
                // 最小连续成功数
                readinessProbe.setSuccessThreshold(1);
                // 超时等待时间
                readinessProbe.setTimeoutSeconds(1);

                // 只有找到对外80开放的服务，才会应用这个探针，否则开了也无效
                if (targetPort != null){
                    v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setReadinessProbe(readinessProbe);
                }
            }

        } catch (Exception e) {
            log.error("[服务发布][Deployment配置]配置失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return v1Deployment;
    }

    /**
     * 增加hPA的控制指标，弹性收缩
     *
     * @param serviceName 服务名
     * @param nameSpace   命名空间
     * @param dolphinsHPA HPA的设置
     * @return V1HorizontalPodAutoscaler对象
     * @throws Exception 标准异常
     */
    public V1HorizontalPodAutoscaler AutoHPAconf(String serviceName, String nameSpace, DolphinsHPA dolphinsHPA) throws Exception {
        V1HorizontalPodAutoscaler v1HorizontalPodAutoscaler = new V1HorizontalPodAutoscaler();
        try {
            // 构建label
            Map<String, String> labels = new HashMap<>();
            labels.put("app", serviceName);
            v1HorizontalPodAutoscaler.setApiVersion("autoscaling/v1");
            v1HorizontalPodAutoscaler.setKind("HorizontalPodAutoscaler");
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(serviceName);
            v1ObjectMeta.setNamespace(nameSpace);
            v1ObjectMeta.setLabels(labels);
            v1HorizontalPodAutoscaler.setMetadata(v1ObjectMeta);

            // 设置spec
            V1HorizontalPodAutoscalerSpec v1HorizontalPodAutoscalerSpec = new V1HorizontalPodAutoscalerSpec();
            V1CrossVersionObjectReference v1CrossVersionObjectReference = new V1CrossVersionObjectReference();
            v1CrossVersionObjectReference.setApiVersion("extensions/v1beta1");
            v1CrossVersionObjectReference.setName(serviceName);
            v1CrossVersionObjectReference.setKind("Deployment");
            v1HorizontalPodAutoscalerSpec.setScaleTargetRef(v1CrossVersionObjectReference);
            // 最大扩容
            v1HorizontalPodAutoscalerSpec.setMaxReplicas(dolphinsHPA.getMaxPod());
            // 最小保留
            v1HorizontalPodAutoscalerSpec.setMinReplicas(dolphinsHPA.getMinPod());
            // 设置CPU扩容指标
            v1HorizontalPodAutoscalerSpec.setTargetCPUUtilizationPercentage(dolphinsHPA.getCpuQuota());

            v1HorizontalPodAutoscaler.setSpec(v1HorizontalPodAutoscalerSpec);
        } catch (Exception e) {
            log.error("[服务发布][HPA配置]配置失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return v1HorizontalPodAutoscaler;
    }

    /**
     * 初始化一个标准服务
     *
     * @throws Exception
     */
    public void initSimpleService(String nameSpace, V1ConfigMap v1ConfigMap,
                                  V1Service v1Service, ExtensionsV1beta1Ingress v1Ingress,
                                  V1HorizontalPodAutoscaler v1HorizontalPodAutoscaler, V1Deployment v1Deployment,
                                  V1Secret v1Secret, V1Secret pullImageSecret) throws Exception {
        try {
            // 引入API
            CoreV1Api apiInstance = new CoreV1Api();
            AutoscalingV1Api autoscalingV1Api = new AutoscalingV1Api();
            ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();

            // 处理ingress逻辑
            try {
                Boolean IngressAlive = false;
                // 获取所有的Ingress
                ExtensionsV1beta1IngressList extensionsV1beta1IngressList = extensionsV1beta1Api.listNamespacedIngress(nameSpace, null, null, null, null, null, null, null, null, null, null);

                for (ExtensionsV1beta1Ingress extensionsV1beta1Ingress : extensionsV1beta1IngressList.getItems()) {
                    if (extensionsV1beta1Ingress.getMetadata().getName().equals(v1Ingress.getMetadata().getName())) {
                        IngressAlive = true;
                    }
                }

                if (IngressAlive) {
                    extensionsV1beta1Api.replaceNamespacedIngress(v1Ingress.getMetadata().getName(), nameSpace, v1Ingress, null, null, null);
                    log.info("[服务部署][Ingress] 服务状态: true, 更新成功!");
                } else {
                    if (v1Ingress != null) {
                        extensionsV1beta1Api.createNamespacedIngress(nameSpace, v1Ingress, null, null, null);
                        log.info("[服务部署][Ingress] 服务状态: false, 创建成功!");
                    }
                }
            } catch (ApiException e) {
                log.error("[服务部署][Ingress]服务处理失败: {}", e.getResponseBody());
            }

            if (v1HorizontalPodAutoscaler != null) {
                try {
                    Boolean HpaAlive = false;
                    V1HorizontalPodAutoscalerList v1HorizontalPodAutoscalerList = autoscalingV1Api.listNamespacedHorizontalPodAutoscaler(nameSpace, null, null, null, null, null, null, null, null, null, null);

                    for (V1HorizontalPodAutoscaler v1HorizontalPodAutoscalerItems : v1HorizontalPodAutoscalerList.getItems()) {
                        if (v1HorizontalPodAutoscalerItems.getMetadata().getName().equals(v1HorizontalPodAutoscaler.getMetadata().getName())) {
                            HpaAlive = true;
                        }
                    }
                    if (HpaAlive) {
                        autoscalingV1Api.replaceNamespacedHorizontalPodAutoscaler(v1HorizontalPodAutoscaler.getMetadata().getName(), nameSpace, v1HorizontalPodAutoscaler, null, null, null);
                        log.info("[服务部署][HPA] 服务状态: true, 更新成功!");
                    } else {
                        autoscalingV1Api.createNamespacedHorizontalPodAutoscaler(nameSpace, v1HorizontalPodAutoscaler, null, null, null);
                        log.info("[服务部署][HPA] 服务状态: false, 创建成功!");
                    }
                } catch (ApiException e) {
                    log.error("[服务部署][HPA]服务处理失败: {}", e.getResponseBody());
                }
            }


            try {
                // 判断服务是否存在
                Boolean ServiceAlive = false;
                // 先检测Service是否存在
                V1ServiceList v1ServiceList = apiInstance.listNamespacedService(nameSpace, null, null, null, null, null, null, null, null, null, null);

                for (V1Service v1ServiceItems : v1ServiceList.getItems()) {
                    if (v1ServiceItems.getMetadata().getName().equals(v1Service.getMetadata().getName())) {
                        ServiceAlive = true;
                    }
                }
                if (ServiceAlive) {
                    // 读取出来，如果是clusterIP模式，无法修改放弃
                    V1Service readV1Service = apiInstance.readNamespacedService(v1Service.getMetadata().getName(), nameSpace, null, null, null);
                    if (!readV1Service.getSpec().getType().equals("ClusterIP")) {
                        apiInstance.replaceNamespacedService(v1Service.getMetadata().getName(), nameSpace, v1Service, "true", null, null);
                        log.info("[服务部署][Service] 服务状态: true, 更新成功!");
                    } else {
                        log.warn("[服务部署][Service] 服务状态: true, 更新失败, 服务Type = ClusterIP!");
                    }
                } else {
                    if (v1Service != null) {
                        apiInstance.createNamespacedService(nameSpace, v1Service, null, null, null);
                        log.info("[服务部署][Service] 服务状态: false, 服务创建成功!");
                    }
                }
            } catch (ApiException e) {
                log.error("[服务部署][Service]服务处理失败: {}", e.getResponseBody());
            }


            if (v1ConfigMap != null) {
                try {
                    // 判断CONFIGMAP是否存在
                    Boolean ConfigMapAlive = false;

                    V1ConfigMapList v1ConfigMapList = apiInstance.listNamespacedConfigMap(nameSpace, null, null, null, null, null, null, null, null, null, null);

                    List<V1ConfigMap> v1ConfigMaps = v1ConfigMapList.getItems();
                    for (V1ConfigMap v1ConfigMapItems : v1ConfigMaps) {
                        if (v1ConfigMapItems.getMetadata().getName().equals(v1ConfigMap.getMetadata().getName())) {
                            ConfigMapAlive = true;
                        }
                    }
                    if (ConfigMapAlive) {
                        apiInstance.replaceNamespacedConfigMap(v1ConfigMap.getMetadata().getName(), nameSpace, v1ConfigMap, "true", null, null);
                        log.info("[服务部署][Configmap] 服务状态: true, 更新成功!");
                    } else {
                        apiInstance.createNamespacedConfigMap(nameSpace, v1ConfigMap, null, null, null);
                        log.info("[服务部署][Configmap] 服务状态: false, 创建成功!");
                    }
                } catch (ApiException e) {
                    log.error("[服务部署][Configmap] 服务处理失败:{}", e.getResponseBody());
                }
            }


            // HTTPS证书&镜像拉取
            try {
                Boolean SecretAuthAlive = false;
                Boolean SecretImagePullAlive = false;

                V1SecretList v1SecretList = apiInstance.listNamespacedSecret(nameSpace, null, null, null, null, null, null, null, null, null, null);

                for (V1Secret v1SecretItems : v1SecretList.getItems()) {
                    if (v1Secret != null) {
                        if (v1SecretItems.getMetadata().getName().equals(v1Secret.getMetadata().getName()) && v1SecretItems.getType().equals("Opaque")) {
                            SecretAuthAlive = true;
                        }
                    }
                    if (pullImageSecret != null) {
                        if (v1SecretItems.getMetadata().getName().equals(pullImageSecret.getMetadata().getName()) && v1SecretItems.getType().equals("kubernetes.io/dockercfg")) {
                            SecretImagePullAlive = true;
                        }
                    }
                }
                if (pullImageSecret != null) {
                    if (SecretImagePullAlive) {
                        apiInstance.deleteNamespacedSecret(pullImageSecret.getMetadata().getName(),nameSpace,null,null,null,null,null,null);
                    }
                    apiInstance.createNamespacedSecret(nameSpace, pullImageSecret, null, null, null);
                    log.info("[服务部署][Secret-PullImages] 服务状态: false, 创建成功!");
                }

                if (v1Secret != null) {
                    if (SecretAuthAlive) {
                        // 先删除再创建新的
                        apiInstance.deleteNamespacedSecret(v1Secret.getMetadata().getName(),nameSpace,null,null,null,null,null,null);
                    }
                    apiInstance.createNamespacedSecret(nameSpace, v1Secret, null, null, null);
                    log.info("[服务部署][Secret-Https] 服务状态: false, 创建成功!");
                }
            } catch (ApiException e) {
                log.error("[服务部署][Secret] 服务处理失败:{}", e.getResponseBody());
            }


            try {
                Boolean DeploymentAlive = false;
                AppsV1Api appsV1Api = new AppsV1Api();
                V1DeploymentList v1DeploymentList = appsV1Api.listNamespacedDeployment(nameSpace, null, null, null, null, null, null, null, null, null, null);

                for (V1Deployment v1DeploymentItems : v1DeploymentList.getItems()) {
                    if (v1DeploymentItems.getMetadata().getName().equals(v1Deployment.getMetadata().getName())) {
                        DeploymentAlive = true;
                    }
                }
                if (DeploymentAlive) {
                    appsV1Api.replaceNamespacedDeployment(v1Deployment.getMetadata().getName(), nameSpace, v1Deployment, null, null, null);
                    log.info("[服务部署][Deployment] 服务状态: true, 更新成功!");
                } else {
                    appsV1Api.createNamespacedDeployment(nameSpace, v1Deployment, null, null, null);
                    log.info("[服务部署][Deployment] 服务状态: false, 部署成功!");
                }
            } catch (ApiException e) {
                log.error("[服务部署][Deployment] 服务处理失败:{}", e.getResponseBody());
            }
        } catch (Exception e) {
            log.error("[服务部署][标准部署]失败:{}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 配置整体服务流水线
     *
     * @return
     * @throws Exception
     */
    public KuberContor configurePipeLine() throws Exception {
        try {
            List<String> serivces = Arrays.asList(
                    "dolphin-gitlab-service", "dolphin-mysql-service",
                    "dolphin-sonar-service", "dolphin-nexus-service");
            for (String serviceName : serivces) {
                // 只要有一个服务没有部署好，就不配置
                if (!this.getNameSpaceService(serviceName, "devops")) {
                    return this;
                }
            }

            /*
             * Sonar的处理部分
             */
            // 创建sonar的token
            String SonarToken;

            try {
                SonarToken = new SonarServ(
                        this.paramets.get("sonar_domain").toString(),
                        "admin", "admin").createToken();
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
                        "http://" + this.paramets.get("gitlab_domain").toString(),
                        "root",
                        this.paramets.get("gitlab_password").toString());
            } catch (Exception e) {
                log.warn("[服务部署][流水线配置]配置GitLab初始化不成功，等待下次再试: {}", e.getMessage());
                return this;
            }

            // 判断是否已经有了用户了
            ArrayList<String> usersList = gitLabServ.getUsersList();

            if (!usersList.contains("dolphins")) {
                // 创建管理员
                gitLabServ.createUser("dolphins",
                        this.paramets.get("gitlab_password").toString(),
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
                        this.paramets.get("nexus_domain").toString(),
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

            // 镜像相关
            this.paramets.put("tcr_token", "admin123");
            this.paramets.put("tcr_username", "admin");
        } catch (Exception e) {
            log.error("[服务部署][配置Pipeline]失败:{}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return this;
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
     * 获取服务状态是否正常发布
     *
     * @param app       应用名
     * @param nameSpace 命名空间
     * @param version   版本
     * @return
     * @throws Exception
     */
    public Boolean getNameSpaceServiceAlive(String app, String nameSpace, String version) throws Exception {
        // 定义服务版本名
        String releaseVersion = null;
        Boolean versionStatus;

        if (version != null) {
            releaseVersion = app + ":" + version;
        }
        try {
            CoreV1Api apiInstance = new CoreV1Api();
            V1PodList list = apiInstance.listNamespacedPod(
                    nameSpace, null, null, null, null, null, null, null, null, null, null);

            ArrayList<String> podNameLists = new ArrayList<>();
            for (V1Pod item : list.getItems()) {
                if (item.getMetadata().getName().startsWith(app)){
                    List<V1ContainerStatus> v1ContainerStatusList = item.getStatus().getContainerStatuses();

                    if (v1ContainerStatusList.size() == 1) {
                        // 判断镜像版本是不是一致的
                        versionStatus = v1ContainerStatusList.get(0).getImage().split("/")[2].equals(releaseVersion);

                        // 如果是就放到列表里
                        if (versionStatus) {
                            podNameLists.add(item.getStatus().getPhase());
                        }
                    }
                }
            }

            // 判断列表里是否每个都是running,如果不是就是false
            for (String items: podNameLists){
                if (!items.equals("Running")){
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("[服务部署][获取服务部署状态]失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 判断服务是否存在
     *
     * @param app
     * @param namespace
     * @return
     * @throws Exception
     */
    protected Boolean aliveNameSpaceService(String app, String namespace) throws Exception {
        try {
            CoreV1Api apiInstance = new CoreV1Api();
            V1PodList list = apiInstance.listNamespacedPod(
                    namespace, null, null,
                    null, null, null,
                    null, null, null,
                    null, null);
            // 遍历list，循环判断pod的状态，并完成pod的调度
            for (V1Pod item : list.getItems()) {
                if (app.equals(item.getMetadata().getLabels().get("app"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("[服务部署][获取服务]失败:{}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return false;
    }


    /**
     * 根据Service Name 获取对应的Ip
     *
     * @param app
     * @param namespace
     * @return
     * @throws Exception
     */
    protected String getNameSpaceServiceIP(String app, String namespace) throws Exception {
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

    /**
     * 获取所有的nameSpace和对应下的服务
     *
     * @return
     * @throws Exception
     */
    public Map<String, Object> NameSpaceServiceLists() throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            CoreV1Api apiInstance = new CoreV1Api();
            AppsV1Api apiInstance2 = new AppsV1Api();
            String pretty = "true";
            V1NamespaceList v1NamespaceList = apiInstance.listNamespace(pretty, null, null, null, null, null, null, null, null, null);
            List<V1Namespace> v1NamespacesLists = v1NamespaceList.getItems();

            for (V1Namespace v1Namespace : v1NamespacesLists) {
                String nameSpace;
                // 忽略特殊命名空间
                if (v1Namespace.getMetadata().getName().equals("kube-node-lease") || v1Namespace.getMetadata().getName().equals("kube-system") || v1Namespace.getMetadata().getName().equals("kube-public") || v1Namespace.getMetadata().getName().equals("defalut")) {
                    continue;
                }

                if (v1Namespace.getMetadata().getName() != null){
                    nameSpace = v1Namespace.getMetadata().getName();
                    if (!results.containsKey(nameSpace)) {
                        results.put(nameSpace, new ArrayList<>());
                    }
                    V1DeploymentList v1DeploymentList = apiInstance2.listNamespacedDeployment(nameSpace, null, null, null, null, null, null, null, null, null, null);
                    List<V1Deployment> v1DeploymentList1 = v1DeploymentList.getItems();
                    ArrayList<String> service = (ArrayList<String>) results.get(nameSpace);

                    for (V1Deployment v1Deployment : v1DeploymentList1) {
                        if (v1Deployment.getMetadata().getName() != null) {
                            service.add(v1Deployment.getMetadata().getName());
                        }
                    }
                    results.put(nameSpace, service);
                }
            }
        } catch (ApiException e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * Yapi的初始化
     * @return
     * @throws Exception
     */
    public KuberContor initYapi() throws Exception{
        try {
            // 如果已经有了就不用再创建了
            if (this.aliveNameSpaceService("dolphin-yapi-service", "devops")) {
                return this;
            }

            CoreV1Api apiInstance = new CoreV1Api();

            V1PersistentVolume v1PersistentVolume = null;
            V1PersistentVolumeClaim v1PersistentVolumeClaim = null;
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    v1PersistentVolume = this.AutoPersistentVolumeAliyun(
                            "dolphin-yapi-service", "devops",
                            160, this.kubIntity.getNfsHost(),
                            this.paramets.get("yapi_nfs_path").toString());

                    v1PersistentVolumeClaim = this.AutoPersistentVolumeClaimAliyun(
                            "dolphin-yapi-service",
                            "devops",
                            160
                    );
                    break;

                case 2:
                    v1PersistentVolume = Yaml.loadAs(new File(this.FilePath + "/yapi/PersistentVolume.yaml"), V1PersistentVolume.class);
                    v1PersistentVolume.getSpec().getCsi().setVolumeHandle(this.kubIntity.getNfsId());
                    Map<String, String> volumeAttr = new HashMap<>();
                    volumeAttr.put("host", this.kubIntity.getNfsHost());
                    volumeAttr.put("path", this.paramets.get("yapi_nfs_path").toString());
                    v1PersistentVolume.getSpec().getCsi().setVolumeAttributes(volumeAttr);
                    v1PersistentVolumeClaim = Yaml.loadAs(new File(this.FilePath + "/yapi/PersistentVolumeClaim.yaml"), V1PersistentVolumeClaim.class);
                    break;
            }

            apiInstance.createPersistentVolume(v1PersistentVolume, null, null, null);
            apiInstance.createNamespacedPersistentVolumeClaim("devops", v1PersistentVolumeClaim, null, null, null);

            List<Object> yapiObject = Yaml.loadAll(new File(this.FilePath + "/yapi/deployment.yaml"));
            for (Object items: yapiObject){
                // 配置V1Service
                if (items.getClass().equals(V1Service.class)) {
                    V1Service v1Service = (V1Service) items;
                    apiInstance.createNamespacedService(
                            "dev", v1Service,
                            null, null,
                            null);
                }

                // 配置V1Deployment
                if (items.getClass().equals(V1Deployment.class)) {
                    V1Deployment v1Deployment = (V1Deployment) items;
                    new AppsV1Api()
                            .createNamespacedDeployment(
                                    "dev", v1Deployment,
                                    null, null,
                                    null);
                }
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 蜜罐服务的初始化
     * @return
     * @throws Exception
     */
    public KuberContor initHfish() throws Exception{
        try {
            // 如果已经有了就不用再创建了
            if (this.aliveNameSpaceService("dolphin-hfish-service", "devops")) {
                return this;
            }

            CoreV1Api apiInstance = new CoreV1Api();

            V1PersistentVolume v1PersistentVolume = null;
            V1PersistentVolumeClaim v1PersistentVolumeClaim = null;
            switch (Integer.parseInt(this.paramets.get("clusterCloud").toString())) {
                case 1:
                    v1PersistentVolume = this.AutoPersistentVolumeAliyun(
                            "dolphin-hfish-service", "devops",
                            160, this.kubIntity.getNfsHost(),
                            this.paramets.get("hfish_nfs_path").toString());

                    v1PersistentVolumeClaim = this.AutoPersistentVolumeClaimAliyun(
                            "dolphin-hfish-service",
                            "devops",
                            160
                    );
                    break;

                case 2:
                    v1PersistentVolume = Yaml.loadAs(new File(this.FilePath + "/hfish/PersistentVolume.yaml"), V1PersistentVolume.class);
                    v1PersistentVolume.getSpec().getCsi().setVolumeHandle(this.kubIntity.getNfsId());
                    Map<String, String> volumeAttr = new HashMap<>();
                    volumeAttr.put("host", this.kubIntity.getNfsHost());
                    volumeAttr.put("path", this.paramets.get("hfish_nfs_path").toString());
                    v1PersistentVolume.getSpec().getCsi().setVolumeAttributes(volumeAttr);
                    v1PersistentVolumeClaim = Yaml.loadAs(new File(this.FilePath + "/hfish/PersistentVolumeClaim.yaml"), V1PersistentVolumeClaim.class);
                    break;
            }

            apiInstance.createPersistentVolume(v1PersistentVolume, null, null, null);
            apiInstance.createNamespacedPersistentVolumeClaim("devops", v1PersistentVolumeClaim, null, null, null);

            List<Object> Objects = Yaml.loadAll(new File(this.FilePath + "/hfish/deployment.yaml"));
            for (Object items: Objects){
                // 配置V1Service
                if (items.getClass().equals(V1Service.class)) {
                    V1Service v1Service = (V1Service) items;
                    apiInstance.createNamespacedService(
                            "dev", v1Service,
                            null, null,
                            null);
                }

                // 配置V1Deployment
                if (items.getClass().equals(V1Deployment.class)) {
                    V1Deployment v1Deployment = (V1Deployment) items;
                    new AppsV1Api()
                            .createNamespacedDeployment(
                                    "dev", v1Deployment,
                                    null, null,
                                    null);
                }
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return this;
    }

    public KuberContor initPromethus() throws Exception {
        try {
            File file = new File(this.FilePath + "/apps/promethus");
            File[] fs = file.listFiles();
            for(File f:fs){					//遍历File[]数组
                if(!f.isDirectory()){
                    try {
                        Yaml.loadAll(f);
                    }catch (Exception e){
//                        Yaml.load(f);
                        System.out.println(f);
                    }
                }
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return this;
    }
}
