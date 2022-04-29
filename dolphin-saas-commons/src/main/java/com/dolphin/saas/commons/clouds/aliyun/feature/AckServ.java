package com.dolphin.saas.commons.clouds.aliyun.feature;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.cs20151215.models.Runtime;
import com.aliyun.cs20151215.models.*;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.service.ClusterService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class AckServ extends MasterServ implements Frame {

    private final Map<String, Object> results = new HashMap<>();
    public com.aliyun.cs20151215.Client client;

    public AckServ(String AccessKeyId, String AccessKeySecret, Map<String, Object> paramets) throws Exception {
        super(AccessKeyId, AccessKeySecret, 1);
        this.paramets = paramets;

        // 访问的域名
        this.config.setEndpoint(
                this.getALIYUN_CLUSTER_URL().replace("{REGIONS}",
                        this.paramets.get("region").toString())
        );
        this.client = new com.aliyun.cs20151215.Client(this.config);
    }

    /**
     * 获取集群的ID
     *
     * @param clusterName 根据集群名称
     * @return
     * @throws Exception
     */
    public String getCluster(String clusterName) throws Exception {
        String clusterId = null;
        try {
            DescribeClustersV1Request describeClustersV1Request = new DescribeClustersV1Request()
                    .setName(clusterName)
                    .setClusterType("ManagedKubernetes");
            // 复制代码运行请自行打印 API 的返回值
            List<DescribeClustersV1ResponseBody.DescribeClustersV1ResponseBodyClusters> clusters = this.client.describeClustersV1(describeClustersV1Request).getBody().getClusters();
            if (clusters.size() > 0){
                clusterId = clusters.get(0).getClusterId();
            }
        } catch (Exception e) {
            log.error("[阿里云SDK][获取历史集群ID]获取失败, 异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return clusterId;
    }

    /**
     * 获取集群链接配置
     * @param clusterId
     * @return
     * @throws Exception
     */
    public String getKubConfig(String clusterId) throws Exception {
        String kubConfig = null;
        try {
            DescribeClusterUserKubeconfigRequest describeClusterUserKubeconfigRequest = new DescribeClusterUserKubeconfigRequest();
            DescribeClusterUserKubeconfigResponse describeClusterUserKubeconfigResponse =  this.client.describeClusterUserKubeconfig(clusterId, describeClusterUserKubeconfigRequest);
            kubConfig = describeClusterUserKubeconfigResponse.getBody().getConfig();
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return kubConfig;
    }

    /**
     * 获取集群创建状态
     *
     * @return
     * @throws Exception
     */
    public Integer getCreateStatus(String clusterName) throws Exception {
        String state = null;
        Integer stauts = 1;
        try {
            DescribeClustersV1Request describeClustersV1Request = new DescribeClustersV1Request()
                    .setName(clusterName)
                    .setClusterType("ManagedKubernetes");
            // 复制代码运行请自行打印 API 的返回值
            List<DescribeClustersV1ResponseBody.DescribeClustersV1ResponseBodyClusters> clusters = this.client.describeClustersV1(describeClustersV1Request).getBody().getClusters();
            if (clusters.size() > 0){
                state = clusters.get(0).getState();
                switch (state) {
                    case "initial":
                        // 创建中
                        stauts = 1;
                        break;
                    case "running":
                        // 运行中
                        stauts = 2;
                        break;
                    case "failed":
                        // 创建失败
                        stauts = 3;
                        break;
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }

        return stauts;
    }

    @Override
    public Frame setVal(Map<String, Object> paramets) {
        return null;
    }

    @Override
    public void initService() throws Exception {
        // 开启ACK PRO集群服务
        try {
            OpenAckServiceRequest openAckServiceRequest = new OpenAckServiceRequest()
                    .setType("propayasgo");
            this.client.openAckService(openAckServiceRequest);
        } catch (Exception e) {
            log.error("[阿里云SDK][开启edge托管集群服务]授权失败: {}", e.getMessage());
        }
    }

    @Override
    public void execService() throws Exception {
        String clusterId = null;
        try {
            clusterId = this.getCluster(this.paramets.get("clusterName").toString());
            if (clusterId == null){
                for (int i=16; i<=31; i++) {
                    try {
                        List<String> switchLists = Arrays.asList(this.paramets.get("switchId").toString());
                        Addon addon0 = new Addon()
                                .setName("flannel");
                        Addon addon1 = new Addon()
                                .setName("csi-plugin");
                        Addon addon2 = new Addon()
                                .setName("csi-provisioner");
                        Addon addon3 = new Addon()
                                .setName("storage-operator")
                                .setConfig("{\"CnfsOssEnable\":\"false\",\"CnfsNasEnable\":\"true\"}");
                        Addon addon4 = new Addon()
                                .setName("nginx-ingress-controller")
                                .setConfig("{\"IngressSlbNetworkType\":\"internet\",\"IngressSlbSpec\":\"slb.s2.small\"}");
                        Addon addon5 = new Addon()
                                .setName("ack-node-local-dns");
                        Addon addon6 = new Addon()
                                .setName("arms-prometheus");

                        // 新增的
                        long masterCount = 0L;
                        long workerCount = 0L;

                        switch (Integer.parseInt(this.paramets.get("concurrency").toString())) {
                            case 1:
                            case 50:
                                masterCount = 1L;
                                workerCount = 2L;
                                break;
                            case 250:
                                masterCount = 1L;
                                workerCount = 3L;
                                break;
                        }
                        // 新增的

                        Runtime runtime = new com.aliyun.cs20151215.models.Runtime()
                                .setName("docker")
                                .setVersion("19.03.15");
                        CreateClusterRequest createClusterRequest = new CreateClusterRequest()
                                .setName(this.paramets.get("clusterName").toString())
                                .setRegionId(this.paramets.get("region").toString())
                                .setClusterType("ManagedKubernetes")
                                .setVpcid(this.paramets.get("vpcId").toString())
                                .setContainerCidr(String.format("172.%s.128.0/20", i))
                                .setServiceCidr(String.format("10.%s.0.0/16", i))
                                .setLoginPassword(this.paramets.get("password").toString())
                                .setMasterVswitchIds(switchLists)
                                .setMasterInstanceTypes((ArrayList) this.paramets.get("masterInstanceLists"))
                                .setAddons(java.util.Arrays.asList(
                                        addon0, addon1, addon2,
                                        addon3, addon4, addon5,
                                        addon6
                                ))
                                .setRuntime(runtime)
                                .setMasterSystemDiskCategory("cloud_efficiency")
                                .setMasterSystemDiskSize(100L)
                                .setMasterCount(masterCount) // 新增的
                                .setNumOfNodes(workerCount) // 新增的
                                .setVswitchIds(switchLists)
                                .setWorkerVswitchIds(switchLists)
                                .setWorkerInstanceTypes((ArrayList) this.paramets.get("nodeInstanceLists"))
                                .setWorkerSystemDiskCategory("cloud_efficiency")
                                .setEndpointPublicAccess(true)
                                .setSecurityGroupId(this.paramets.get("securityGroupId").toString())
                                .setDisableRollback(true)
                                .setSnatEntry(true)
                                .setClusterSpec("ack.pro.small")
                                .setNatGateway(true)
                                .setWorkerSystemDiskSize(100L);
                        if (Integer.parseInt(this.paramets.get("buy").toString()) == 1) {
                            createClusterRequest.setMasterInstanceChargeType("PrePaid")
                                    .setMasterPeriodUnit("Month")
                                    .setMasterPeriod(1L)
                                    .setMasterAutoRenew(true)
                                    .setMasterAutoRenewPeriod(1L)
                                    .setWorkerInstanceChargeType("PrePaid")
                                    .setWorkerPeriodUnit("Month")
                                    .setWorkerPeriod(1L)
                                    .setWorkerAutoRenew(true)
                                    .setWorkerAutoRenewPeriod(1L);
                        }
                        log.info("[集群]创建参数: {}", JSON.toJSON(createClusterRequest));
                        clusterId = client.createCluster(createClusterRequest).getBody().getClusterId();
                        log.info("[阿里云SDK][创建托管集群]创建成功, 集群ID: {}", clusterId);
                        break;
                    }catch (Exception e){
                        log.warn("[阿里云SDK][创建托管集群]失败,更换地址再试: {}",e.getMessage());
                    }
                }
            }
            this.results.put("clusterId", clusterId);
        } catch (Exception e) {
            log.error("[阿里云SDK][创建托管集群]创建失败, 异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }

    }

    @Override
    public void finishService() throws Exception {

    }

    @Override
    public void run() throws Exception {
        ClusterService clusterService = (ClusterService) this.paramets.get("clusterService");
        Long cid = Long.parseLong(this.paramets.get("cid").toString());
        RedisCommonUtils redisCommonUtils = (RedisCommonUtils) this.paramets.get("redisCommonUtils");

        if (!redisCommonUtils.hasKeys("AckServ." + cid)) {
            redisCommonUtils.noExpireSset("AckServ." + cid, 1);
            try {
                clusterService.UpdateStage(5, cid, 1);
                this.initService();
                this.execService();
                clusterService.UpdateStage(5, cid, 2);
            } catch (Exception e) {
                clusterService.UpdateStage(5, cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.getRedisTemplate().delete("AckServ." + cid);
            }
        }
    }

    @Override
    public Frame runner() throws Exception {
        this.run();
        return this;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        return this.results;
    }

    /**
     * 获取集群的外部访问地址
     * @param clusterId
     * @return
     * @throws Exception
     */
    public String getKubIpAddr(String clusterId) throws Exception {
        String kubAddr = null;
        try {
            String MasterUrl = client.describeClusterDetail(clusterId).getBody().getMasterUrl();
            Map<String, String> MasterObj = JSON.parseObject(MasterUrl, Map.class);
            kubAddr = MasterObj.get("api_server_endpoint");
        }catch (Exception e){
            log.error("[阿里云SDK][获取Kub地址]失败: {}", e.getMessage());
        }
        return kubAddr;
    }
}
