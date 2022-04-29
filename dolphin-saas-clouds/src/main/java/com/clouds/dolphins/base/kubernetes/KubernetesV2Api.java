package com.clouds.dolphins.base.kubernetes;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Yaml;
import io.kubernetes.client.util.credentials.AccessTokenAuthentication;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class KubernetesV2Api {
    private final KubIntity kubIntity;

    /**
     * 基础配置，自动配置
     */
    public KubernetesV2Api(KubIntity kubIntity) {
        this.kubIntity = kubIntity;
        ApiClient client = new ClientBuilder().setBasePath(this.kubIntity.getApiServer()).setVerifyingSsl(false)
                .setAuthentication(new AccessTokenAuthentication(this.kubIntity.getToken())).build();
        Configuration.setDefaultApiClient(client);
    }

    /**
     * 初始化namespace
     *
     * @return
     */
    public void initNameSpace() throws Exception {
        // 初始化namespace
        List<String> nameSpace = Arrays.asList("test", "dev", "demos", "online", "devops");
        try {
            for (String s : nameSpace) {
                CoreV1Api apiInstance = new CoreV1Api();
                // 13.0.0
                V1Namespace v1Namespace = new V1Namespace();
                v1Namespace.setApiVersion("v1");
                v1Namespace.setKind("Namespace");
                V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
                v1ObjectMeta.setName(s);
                Map<String, String> Lab = new HashMap<>();
                Lab.put("name", s);
                v1ObjectMeta.setLabels(Lab);
                v1Namespace.setMetadata(v1ObjectMeta);
                apiInstance.createNamespace(v1Namespace, null, null, null);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 设置基础服务通用的mysql
     *
     * @param PassWord 密码
     * @throws Exception
     */
    public void initMysql(String nameSpace, String PassWord) throws Exception {
        try {
            CoreV1Api apiInstance = new CoreV1Api();
            V1PersistentVolume v1PersistentVolume = Yaml.loadAs(new File("/yaml/mysql/PersistentVolume.yaml"), V1PersistentVolume.class);
            ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();

            // 修改域名信息
            ExtensionsV1beta1Ingress v1Ingress = Yaml.loadAs(new File("/yaml/mysql/Ingress.yaml"), ExtensionsV1beta1Ingress.class);
            v1Ingress.getSpec().getRules().get(0).setHost("mysql.test.com");

            // 修改pcv信息
            v1PersistentVolume.getSpec().getCsi().setVolumeHandle(this.kubIntity.getNfsId());
            Map<String, String> volumeAttr = new HashMap<>();
            volumeAttr.put("host", this.kubIntity.getNfsHost());
            volumeAttr.put("path", "/mysql");
            v1PersistentVolume.getSpec().getCsi().setVolumeAttributes(volumeAttr);
            V1PersistentVolumeClaim v1PersistentVolumeClaim = Yaml.loadAs(new File("/yaml/mysql/PersistentVolumeClaim.yaml"), V1PersistentVolumeClaim.class);
            V1Service v1Service = Yaml.loadAs(new File("/yaml/mysql/Service.yaml"), V1Service.class);
            V1Deployment v1Deployment = Yaml.loadAs(new File("/yaml/mysql/deployment.yaml"), V1Deployment.class);

            // 修改deployment里设置的默认密码、访问域名
            String replaceInfo = v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).getValue().replace("<PASSWORD>", PassWord);
            v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).setValue(replaceInfo);
            apiInstance.createPersistentVolume(v1PersistentVolume, null, null, null);
            apiInstance.createNamespacedPersistentVolumeClaim(nameSpace, v1PersistentVolumeClaim, null, null, null);
            extensionsV1beta1Api.createNamespacedIngress(nameSpace, v1Ingress, null, null, null);
            apiInstance.createNamespacedService(nameSpace, v1Service, null, null, null);
            new AppsV1Api().createNamespacedDeployment(nameSpace, v1Deployment, null, null, null);
        } catch (Exception e) {
            log.error("initMysql:" + e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 部署gitlab项目
     *
     * @param nameSpace 命名空间
     * @param PassWord  密码
     * @param Domain    域名
     * @throws Exception
     */
    public void initGitLab(String nameSpace, String PassWord, String Domain) throws Exception {
        try {
            CoreV1Api apiInstance = new CoreV1Api();
            ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();
            V1PersistentVolume v1PersistentVolume = Yaml.loadAs(new File("/yaml/gitlab/PersistentVolume.yaml"), V1PersistentVolume.class);
            // 修改域名信息
            ExtensionsV1beta1Ingress v1Ingress = Yaml.loadAs(new File("/yaml/gitlab/Ingress.yaml"), ExtensionsV1beta1Ingress.class);
            v1Ingress.getSpec().getRules().get(0).setHost("gitlab.test.com");

            // 修改pcv信息
            v1PersistentVolume.getSpec().getCsi().setVolumeHandle(this.kubIntity.getNfsId());
            Map<String, String> volumeAttr = new HashMap<>();
            volumeAttr.put("host", this.kubIntity.getNfsHost());
            volumeAttr.put("path", "/gitlab");
            v1PersistentVolume.getSpec().getCsi().setVolumeAttributes(volumeAttr);
            V1PersistentVolumeClaim v1PersistentVolumeClaim = Yaml.loadAs(new File("/yaml/gitlab/PersistentVolumeClaim.yaml"), V1PersistentVolumeClaim.class);
            V1Service v1Service = Yaml.loadAs(new File("/yaml/gitlab/Service.yaml"), V1Service.class);
            V1Deployment v1Deployment = Yaml.loadAs(new File("/yaml/gitlab/deployment.yaml"), V1Deployment.class);

            // 修改deployment里设置的默认密码、访问域名
            String replaceInfo = v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(1).getValue().replace("<PassWord>", PassWord)
                    .replace("<Domain>", Domain);
            v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(1).setValue(replaceInfo);
            apiInstance.createPersistentVolume(v1PersistentVolume, null, null, null);
            apiInstance.createNamespacedPersistentVolumeClaim(nameSpace, v1PersistentVolumeClaim, null, null, null);
            extensionsV1beta1Api.createNamespacedIngress(nameSpace, v1Ingress, null, null, null);
            apiInstance.createNamespacedService(nameSpace, v1Service, null, null, null);
            new AppsV1Api().createNamespacedDeployment(nameSpace, v1Deployment, null, null, null);
        } catch (Exception e) {
            log.error("initGitLab: " + e);
            throw new Exception(e.getMessage());
        }
    }


    /**
     * 部署sonar项目
     *
     * @return
     */
    public void initSonar(String nameSpace, Map<String, Object> results) throws Exception {
        try {
            while (true) {
                // 创建数据库
                try {
                    Connection connection = null;
                    String databaseUrl = null;

                    Class.forName("com.mysql.jdbc.Driver");//添加一个驱动类
                    String SQL = "create database sonar";
                    databaseUrl = "jdbc:mysql://" + results.get("MySQLIp").toString() + ":3306?useUnicode=true&chararcterEncoding=utf8";//设置mysql数据库的地址
                    connection = DriverManager.getConnection(databaseUrl, "root", results.get("mysqlPassWord").toString());
                    connection.createStatement().execute(SQL);
                    break;
                } catch (Exception e) {
                    Thread.sleep(5000);
                }
            }

            CoreV1Api apiInstance = new CoreV1Api();
            ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();
            // 修改域名信息
            ExtensionsV1beta1Ingress v1Ingress = Yaml.loadAs(new File("/yaml/sonar/Ingress.yaml"), ExtensionsV1beta1Ingress.class);
            v1Ingress.getSpec().getRules().get(0).setHost("sonar.test.com");

            V1Service v1Service = Yaml.loadAs(new File("/yaml/sonar/Service.yaml"), V1Service.class);
            V1Deployment v1Deployment = Yaml.loadAs(new File("/yaml/sonar/deployment.yaml"), V1Deployment.class);
            List<V1EnvVar> v1EnvVars = v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
            v1EnvVars.get(0).setValue(v1EnvVars.get(0).getValue().replace("<HOSTS>", results.get("MySQLIp").toString()));
            v1EnvVars.get(1).setValue(v1EnvVars.get(1).getValue().replace("<PASSWORD>", results.get("mysqlPassWord").toString()));
            v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setEnv(v1EnvVars);

            extensionsV1beta1Api.createNamespacedIngress(nameSpace, v1Ingress, null, null, null);
            apiInstance.createNamespacedService(nameSpace, v1Service, null, null, null);
            new AppsV1Api().createNamespacedDeployment(nameSpace, v1Deployment, null, null, null);
        } catch (Exception e) {
            log.error("initSonar: " + e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 部署jenkins项目
     *
     * @return
     */
    public void initJenkins(String nameSpace, List<V1HostAlias> v1HostAliasList, JenkinsIntity initJenkinsKV) throws Exception {
        try {
            CoreV1Api apiInstance = new CoreV1Api();
            ExtensionsV1beta1Api extensionsV1beta1Api = new ExtensionsV1beta1Api();
            V1PersistentVolume v1PersistentVolume = Yaml.loadAs(new File("/yaml/jenkins/PersistentVolume.yaml"), V1PersistentVolume.class);
            // 修改域名信息
            ExtensionsV1beta1Ingress v1Ingress = Yaml.loadAs(new File("/yaml/jenkins/Ingress.yaml"), ExtensionsV1beta1Ingress.class);
            v1Ingress.getSpec().getRules().get(0).setHost("jenkins.test.com");

            // 修改pcv信息
            v1PersistentVolume.getSpec().getCsi().setVolumeHandle(this.kubIntity.getNfsId());
            Map<String, String> volumeAttr = new HashMap<>();
            volumeAttr.put("host", this.kubIntity.getNfsHost());
            volumeAttr.put("path", "/jenkins");
            v1PersistentVolume.getSpec().getCsi().setVolumeAttributes(volumeAttr);

            V1PersistentVolumeClaim v1PersistentVolumeClaim = Yaml.loadAs(new File("/yaml/jenkins/PersistentVolumeClaim.yaml"), V1PersistentVolumeClaim.class);
            V1Service v1Service = Yaml.loadAs(new File("/yaml/jenkins/Service.yaml"), V1Service.class);
            V1Deployment v1Deployment = Yaml.loadAs(new File("/yaml/jenkins/deployment.yaml"), V1Deployment.class);
            v1Deployment.getSpec().getTemplate().getSpec().setHostAliases(v1HostAliasList);

            // 初始化
            apiInstance.createPersistentVolume(v1PersistentVolume, null, null, null);
            apiInstance.createNamespacedPersistentVolumeClaim(nameSpace, v1PersistentVolumeClaim, null, null, null);
            extensionsV1beta1Api.createNamespacedIngress(nameSpace, v1Ingress, null, null, null);
            apiInstance.createNamespacedService(nameSpace, v1Service, null, null, null);

            // 初始化关键参数
            List<V1EnvVar> v1EnvVars = v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
            v1EnvVars.get(0).setValue(v1EnvVars.get(0).getValue().replace("<PASSWORD>", initJenkinsKV.getJenkinsPassword()));
            v1EnvVars.get(1).setValue(v1EnvVars.get(1).getValue().replace("<SONAR_TOKEN>", initJenkinsKV.getSonarToken()));
            v1EnvVars.get(2).setValue(v1EnvVars.get(2).getValue().replace("<GITLAB_TOKEN>", initJenkinsKV.getGitlabToken()));
            v1EnvVars.get(3).setValue(v1EnvVars.get(3).getValue().replace("<TCR_USERNAME>", initJenkinsKV.getTcrUserName()));
            v1EnvVars.get(4).setValue(v1EnvVars.get(4).getValue().replace("<TCR_TOKEN>", initJenkinsKV.getTcrToken()));
            v1EnvVars.get(5).setValue(v1EnvVars.get(5).getValue().replace("<SONAR_SERVER_URL>", initJenkinsKV.getSonarServerUrl()));
            v1EnvVars.get(6).setValue(v1EnvVars.get(6).getValue().replace("<K8S_ADDR>", initJenkinsKV.getK8sApiServer()));
            v1EnvVars.get(7).setValue(v1EnvVars.get(7).getValue().replace("<JENKINS_ADDR>", "http://" + this.getNamespaceServiceIP("dolphin-jenkins-service")));
            v1EnvVars.get(8).setValue(v1EnvVars.get(8).getValue().replace("<GITLAB_PASSWORD>", initJenkinsKV.getGitlabPassword()));

            v1Deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setEnv(v1EnvVars);

            new AppsV1Api().createNamespacedDeployment(nameSpace, v1Deployment, null, null, null);
        } catch (Exception e) {
            log.error("initJenkins: " + e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 获取所有pod信息
     *
     * @return
     * @throws Exception
     */
    public Boolean getNameSpaceService(String app) throws Exception {
        try {
            CoreV1Api apiInstance = new CoreV1Api();
            V1PodList list = apiInstance.listNamespacedPod("devops", null, null, null, null, null, null, null, null, null, null);
            // 遍历list，循环判断pod的状态，并完成pod的调度
            for (V1Pod item : list.getItems()) {
                if (app.equals(item.getMetadata().getLabels().get("app")) && item.getStatus().getPhase().equals("Running")) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("getNameSpaceService: " + e);
            throw new Exception(e.getMessage());
        }
        return false;
    }

    /**
     * 根据Service Name 获取对应的Ip
     *
     * @param serviceName service名称
     * @return
     * @throws Exception
     */
    public String getNamespaceServiceIP(String serviceName) throws Exception {
        String IP = "";
        while (true) {
            CoreV1Api apiInstance = new CoreV1Api();
            V1Service v1Service = apiInstance.readNamespacedService(serviceName, "devops", null, null, null);
            try {
                IP = v1Service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
                break;
            } catch (Exception e) {
                Thread.sleep(5000);
            }
        }

        return IP;
    }
}
