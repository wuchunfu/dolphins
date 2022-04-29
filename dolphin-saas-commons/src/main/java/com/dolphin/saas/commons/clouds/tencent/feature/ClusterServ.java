package com.dolphin.saas.commons.clouds.tencent.feature;

import com.dolphin.saas.commons.clouds.tencent.entity.TkeCluster;
import com.dolphin.saas.commons.clouds.tencent.globs.Serv;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.profile.Language;
import com.tencentcloudapi.tke.v20180525.TkeClient;
import com.tencentcloudapi.tke.v20180525.models.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ClusterServ extends Serv {

    private final ClientProfile clientProfile;

    public ClusterServ(Credential cred, HttpProfile httpProfile) {
        super(cred, httpProfile);
        this.clientProfile = new ClientProfile();
    }

    /**
     * 获取有用的网段
     *
     * @param reGion 归属
     * @param vpcId  vpcID
     * @return 网段地址
     */
    public String calcCIDRIpAddr(String reGion, String vpcId) {
        List<String> IpAddr = Arrays.asList(
                "172.16.0.0/16", "172.17.0.0/16",
                "172.18.0.0/16", "172.19.0.0/16",
                "172.20.0.0/16", "172.21.0.0/16",
                "172.22.0.0/16", "172.23.0.0/16",
                "172.24.0.0/16", "172.25.0.0/16",
                "172.26.0.0/16", "172.27.0.0/16",
                "172.28.0.0/16", "172.29.0.0/16",
                "172.30.0.0/16", "172.31.0.0/16");
        for (String s : IpAddr) {
            try {
                this.getHttpProfile().setEndpoint(this.getTKE_URL());
                this.clientProfile.setHttpProfile(this.getHttpProfile());
                TkeClient client = new TkeClient(this.getCred(), reGion, this.clientProfile);
                DescribeRouteTableConflictsRequest req = new DescribeRouteTableConflictsRequest();
                req.setRouteTableCidrBlock(s);
                req.setVpcId(vpcId);
                DescribeRouteTableConflictsResponse resp = client.DescribeRouteTableConflicts(req);
                // 输出json格式的字符串回包
                if (!resp.getHasConflict()) {
                    return s;
                }
            } catch (TencentCloudSDKException e) {
                log.error("calcCIDRIpAddr:" + e);
            }
        }
        return null;
    }

    /**
     * 查集群是否存在
     *
     * @param reGion
     * @return
     * @throws TencentCloudSDKException
     */
    public ArrayList<String> clusterAlive(String reGion) throws TencentCloudSDKException {
        ArrayList<String> clusterIds = new ArrayList<>();
        try {
            this.getHttpProfile().setEndpoint(this.getTKE_URL());
            this.clientProfile.setHttpProfile(this.getHttpProfile());
            TkeClient client = new TkeClient(this.getCred(), reGion, this.clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeClustersRequest req = new DescribeClustersRequest();
            DescribeClustersResponse resp = client.DescribeClusters(req);
            if (resp.getClusters().length > 0) {
                for (Cluster cluster : resp.getClusters()) {
                    clusterIds.add(cluster.getClusterId());
                }
            }
        } catch (TencentCloudSDKException e) {
            log.error("clusterAlive:" + e);
            throw new TencentCloudSDKException(e.getMessage());
        }
        return clusterIds;
    }

    /**
     * 获取集群的密钥信息
     *
     * @param clusterId 集群id
     * @param reGion    归属
     * @return
     */
    public Map<String, Object> clusterSecurityKey(String clusterId, String reGion) throws TencentCloudSDKException {
        Map<String, Object> results = new HashMap<>();
        try {
            this.getHttpProfile().setEndpoint(this.getTKE_URL());
            this.clientProfile.setHttpProfile(this.getHttpProfile());
            TkeClient client = new TkeClient(getCred(), reGion, this.clientProfile);
            DescribeClusterSecurityRequest describeClusterSecurityRequest = new DescribeClusterSecurityRequest();
            describeClusterSecurityRequest.setClusterId(clusterId);
            DescribeClusterSecurityResponse describeClusterSecurityResponse = client.DescribeClusterSecurity(describeClusterSecurityRequest);
            if (describeClusterSecurityResponse.getKubeconfig() != null) {
                String token = describeClusterSecurityResponse.getKubeconfig().split("\n")[17].split(":")[1].trim();
                results.put("kubconfig", describeClusterSecurityResponse.getKubeconfig());
                results.put("username", describeClusterSecurityResponse.getUserName());
                results.put("password", describeClusterSecurityResponse.getPassword());
                results.put("ip", describeClusterSecurityResponse.getPgwEndpoint());
                results.put("domain", describeClusterSecurityResponse.getDomain());
                results.put("token", token);
            } else {
                throw new TencentCloudSDKException("获取集群密钥失败!");
            }
        } catch (TencentCloudSDKException e) {
            log.error("clusterSecurityKey:" + e);
            throw new TencentCloudSDKException(e.getMessage());
        }
        return results;
    }

    /**
     * 开启内外网端口
     *
     * @param reGion    归属
     * @param clusterId 集群id
     * @param subnetId  子网id
     * @throws TencentCloudSDKException
     */
    public void clusterNetworkOpen(String reGion, String clusterId, String subnetId) throws TencentCloudSDKException {
        try {
            this.getHttpProfile().setEndpoint(getTKE_URL());
            this.clientProfile.setHttpProfile(this.getHttpProfile());
            TkeClient client = new TkeClient(this.getCred(), reGion, this.clientProfile);

            /*
             * 开内网访问
             */
            CreateClusterEndpointRequest req1 = new CreateClusterEndpointRequest();
            req1.setClusterId(clusterId);
            req1.setSubnetId(subnetId);
            req1.setIsExtranet(false);
            client.CreateClusterEndpoint(req1);

            /*
             * 创建外联网端点
             */
            CreateClusterEndpointVipRequest req2 = new CreateClusterEndpointVipRequest();
            req2.setClusterId(clusterId);
            String[] securityPolicies1 = {"0.0.0.0/0"};
            req2.setSecurityPolicies(securityPolicies1);
            client.CreateClusterEndpointVip(req2);
        } catch (TencentCloudSDKException e) {
            log.error("clusterNetworkOpen:" + e);
            throw new TencentCloudSDKException(e.getMessage());
        }
    }

    /**
     * 创建托管集群
     *
     * @param tkeCluster tke集群的配置
     * @return 集群的id(clusterId)
     */
    public Map<String, Object> createTkeCluster(TkeCluster tkeCluster) throws TencentCloudSDKException {
        Map<String, Object> results = new HashMap<>();
        try {
            this.getHttpProfile().setEndpoint(getTKE_URL());
            this.clientProfile.setHttpProfile(this.getHttpProfile());
            TkeClient client = new TkeClient(this.getCred(), tkeCluster.getReGion(), this.clientProfile);

            CreateClusterRequest createClusterRequest = new CreateClusterRequest();
            RunInstancesForNode[] runInstancesForNodes1 = new RunInstancesForNode[1];
            RunInstancesForNode runInstancesForNode1 = new RunInstancesForNode();
            runInstancesForNode1.setNodeRole("WORKER");
            // 存放CVM的JSON
            List<String> tkeCvmInfo = tkeCluster.getCvmJson();
            String[] runInstancesPara1 = tkeCvmInfo.toArray(new String[tkeCvmInfo.size()]);
            runInstancesForNode1.setRunInstancesPara(runInstancesPara1);
            runInstancesForNodes1[0] = runInstancesForNode1;
            createClusterRequest.setRunInstancesForNode(runInstancesForNodes1);

            ClusterCIDRSettings clusterCIDRSettings1 = new ClusterCIDRSettings();
            clusterCIDRSettings1.setClusterCIDR(tkeCluster.getClusterCIDR());
            clusterCIDRSettings1.setMaxNodePodNum(Long.valueOf(tkeCluster.getMaxNodePodNum()));
            clusterCIDRSettings1.setMaxClusterServiceNum(Long.valueOf(tkeCluster.getMaxClusterServiceNum()));
            createClusterRequest.setClusterCIDRSettings(clusterCIDRSettings1);

            ClusterBasicSettings clusterBasicSettings1 = new ClusterBasicSettings();
            // 这个系统有坑，换成centos
//            clusterBasicSettings1.setClusterOs("tlinux2.4x86_64");
            clusterBasicSettings1.setClusterOs("centos7.2x86_64");

            clusterBasicSettings1.setClusterVersion("1.18.4");
//            clusterBasicSettings1.setClusterVersion("1.20.6");
            clusterBasicSettings1.setClusterName("海豚工程DevOps生产集群");
            clusterBasicSettings1.setClusterDescription("海豚工程-SaaS构建的业务集群");
            clusterBasicSettings1.setVpcId(tkeCluster.getVpcId());
            TagSpecification[] tagSpecifications1 = new TagSpecification[1];
            TagSpecification tagSpecification1 = new TagSpecification();
            tagSpecification1.setResourceType("cluster");
            tagSpecifications1[0] = tagSpecification1;
            clusterBasicSettings1.setTagSpecification(tagSpecifications1);
            clusterBasicSettings1.setOsCustomizeType("GENERAL");
            clusterBasicSettings1.setNeedWorkSecurityGroup(true);
            createClusterRequest.setClusterBasicSettings(clusterBasicSettings1);
            ClusterAdvancedSettings clusterAdvancedSettings1 = new ClusterAdvancedSettings();
            clusterAdvancedSettings1.setIPVS(false);
            clusterAdvancedSettings1.setAsEnabled(false);
            clusterAdvancedSettings1.setDeletionProtection(false);
            clusterAdvancedSettings1.setAuditEnabled(true);
            clusterAdvancedSettings1.setRuntimeVersion("19.3");
            clusterAdvancedSettings1.setEnableCustomizedPodCIDR(false);
            createClusterRequest.setClusterAdvancedSettings(clusterAdvancedSettings1);
            InstanceAdvancedSettings instanceAdvancedSettings1 = new InstanceAdvancedSettings();
            instanceAdvancedSettings1.setDockerGraphPath("/var/lib/docker");
            createClusterRequest.setInstanceAdvancedSettings(instanceAdvancedSettings1);
            createClusterRequest.setClusterType("MANAGED_CLUSTER");

            ExtensionAddon[] extensionAddons1 = new ExtensionAddon[5];
            ExtensionAddon extensionAddon1 = new ExtensionAddon();
            extensionAddon1.setAddonName("NodeProblemDetectorPlus");
            extensionAddon1.setAddonParam("{\"kind\":\"NodeProblemDetector\",\"apiVersion\":\"platform.tke/v1\",\"metadata\":{\"generateName\":\"npd\"},\"spec\":{\"version\":\"v2.0.0\",\"selfCure\":true,\"uin\":\"100002260652\",\"subUin\":\"100002260652\",\"policys\":[{\"actions\":{\"CVM\":{\"reBootCVM\":true,\"retryCounts\":1},\"runtime\":{\"reStartDokcer\":true,\"reStartKubelet\":true,\"retryCounts\":1}},\"conditionType\":\"Ready\"}]}}");
            extensionAddons1[0] = extensionAddon1;

            ExtensionAddon extensionAddon2 = new ExtensionAddon();
            extensionAddon2.setAddonName("OOMGuard");
            extensionAddon2.setAddonParam("{\"kind\":\"OOMGuard\",\"apiVersion\":\"platform.tke/v1\",\"metadata\":{\"generateName\":\"oom\"},\"spec\":{}}");
            extensionAddons1[1] = extensionAddon2;

            // cbs的组件
            ExtensionAddon extensionAddon3 = new ExtensionAddon();
            extensionAddon3.setAddonName("CBS");
            extensionAddon3.setAddonParam("{\"kind\":\"CBS\",\"apiVersion\":\"platform.tke/v1\",\"metadata\":{\"generateName\":\"cbs\"},\"spec\":{}}");
            extensionAddons1[2] = extensionAddon3;

            // cos的组件
            ExtensionAddon extensionAddon4 = new ExtensionAddon();
            extensionAddon4.setAddonName("COS");
            extensionAddon4.setAddonParam("{\"kind\":\"COS\",\"apiVersion\":\"platform.tke/v1\",\"metadata\":{\"generateName\":\"cos\"},\"spec\":{\"version\":\"1.0.0\"}}");
            extensionAddons1[3] = extensionAddon4;

            // cfs的组件
            ExtensionAddon extensionAddon5 = new ExtensionAddon();
            extensionAddon5.setAddonName("CFS");
            extensionAddon5.setAddonParam("{\"kind\":\"CFS\",\"apiVersion\":\"platform.tke/v1\",\"metadata\":{\"generateName\":\"cfs\"},\"spec\":{\"version\":\"1.0.0\"}}");
            extensionAddons1[4] = extensionAddon5;

            createClusterRequest.setExtensionAddons(extensionAddons1);

            CreateClusterResponse createClusterResponse = client.CreateCluster(createClusterRequest);
            results.put("clusterId", createClusterResponse.getClusterId());
        } catch (TencentCloudSDKException e) {
            log.error("createTkeCluster:" + e);
            throw new TencentCloudSDKException(e.getMessage());
        }

        return results;
    }

    /**
     * 获取集群的状态
     *
     * @param reGion    归属
     * @param clusterId 集群id
     * @return
     */
    public Boolean clusterStatus(String reGion, String clusterId) throws TencentCloudSDKException {
        try {
            this.getHttpProfile().setEndpoint(getTKE_URL());
            this.clientProfile.setHttpProfile(this.getHttpProfile());
            this.clientProfile.setLanguage(Language.ZH_CN);
            TkeClient client = new TkeClient(this.getCred(), reGion, this.clientProfile);
            DescribeClustersRequest describeClustersRequest = new DescribeClustersRequest();
            String[] clusterIds1 = {clusterId};
            describeClustersRequest.setClusterIds(clusterIds1);
            DescribeClustersResponse describeClustersResponse = client.DescribeClusters(describeClustersRequest);
            for (Cluster cluster : describeClustersResponse.getClusters()) {
                return cluster.getClusterStatus().equals("Running");
            }
        } catch (TencentCloudSDKException e) {
            log.error("clusterStatus:" + e);
            throw new TencentCloudSDKException(e.getMessage());
        }

        return false;
    }

    /**
     * 获取对应集群的节点ip(单个ip)
     *
     * @param reGion    归属
     * @param clusterId 集群id
     * @return
     */
    public Map<String, Object> clusterServerIp(String reGion, String clusterId) throws TencentCloudSDKException {
        Map<String, Object> results = new HashMap<>();
        try {
            this.getHttpProfile().setEndpoint(getTKE_URL());
            this.clientProfile.setHttpProfile(this.getHttpProfile());
            TkeClient client = new TkeClient(this.getCred(), reGion, this.clientProfile);
            DescribeExistedInstancesRequest describeExistedInstancesRequest = new DescribeExistedInstancesRequest();
            describeExistedInstancesRequest.setClusterId(clusterId);
            DescribeExistedInstancesResponse describeExistedInstancesResponse = client.DescribeExistedInstances(describeExistedInstancesRequest);
            if (describeExistedInstancesResponse.getTotalCount() < 1) {
                throw new TencentCloudSDKException("当前集群异常,没有节点!");
            }
            ArrayList<String> ipAddr = new ArrayList<>();
            ExistedInstance[] existedInstances = describeExistedInstancesResponse.getExistedInstanceSet();
            for (ExistedInstance existedInstance : existedInstances) {
                if (existedInstance.getAlreadyInCluster() != null) {
                    if (Objects.equals(existedInstance.getAlreadyInCluster(), clusterId)) {
                        if (existedInstance.getPublicIpAddresses().length > 0) {
                            ipAddr.add(existedInstance.getPublicIpAddresses()[0]);
                        }
                    }
                }
            }
            if (ipAddr.size() > 0) {
                results.put("ip", ipAddr.get(0));
            }
        } catch (TencentCloudSDKException e) {
            log.error("clusterServerIp:" + e);
            throw new TencentCloudSDKException(e.getMessage());
        }
        return results;
    }

    /**
     * 获取集群下所有CVM节点的实例id
     *
     * @param reGion    归属
     * @param clusterId 集群id
     * @return
     */
    public ArrayList<String> clusterServerNodeInstanceId(String reGion, String clusterId) {
        ArrayList<String> results = new ArrayList<>();
        try {
            this.getHttpProfile().setEndpoint(getTKE_URL());
            this.clientProfile.setHttpProfile(this.getHttpProfile());
            TkeClient client = new TkeClient(this.getCred(), reGion, this.clientProfile);
            DescribeExistedInstancesRequest describeExistedInstancesRequest = new DescribeExistedInstancesRequest();
            describeExistedInstancesRequest.setClusterId(clusterId);
            DescribeExistedInstancesResponse describeExistedInstancesResponse = client.DescribeExistedInstances(describeExistedInstancesRequest);
            if (describeExistedInstancesResponse.getTotalCount() < 1) {
                throw new TencentCloudSDKException("当前没有节点!");
            } else {
                ExistedInstance[] existedInstances = describeExistedInstancesResponse.getExistedInstanceSet();
                for (ExistedInstance existedInstance : existedInstances) {
                    if (existedInstance.getAlreadyInCluster() != null) {
                        if (Objects.equals(existedInstance.getAlreadyInCluster(), clusterId)) {
                            results.add(existedInstance.getInstanceId());
                        }
                    }
                }
            }
        } catch (TencentCloudSDKException e) {
            log.error("clusterServerNodeInstanceId:" + e);
        }
        return results;
    }
}
