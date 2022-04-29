package com.dolphin.saas.commons.clouds.tencent;

import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.commons.clouds.tencent.entity.Cvm;
import com.dolphin.saas.commons.clouds.tencent.feature.CvmServ;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.profile.Language;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.DescribeInstancesStatusRequest;
import com.tencentcloudapi.cvm.v20170312.models.DescribeInstancesStatusResponse;
import com.tencentcloudapi.cvm.v20170312.models.InstanceStatus;
import com.tencentcloudapi.tke.v20180525.TkeClient;
import com.tencentcloudapi.tke.v20180525.models.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class ClusterServPlugin extends MasterServ implements Frame {

    // 集群ID
    private String clusterId;

    /**
     * 加载厂商
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     */
    public ClusterServPlugin(String AccessKeyId, String AccessKeySecret) {
        super(AccessKeyId, AccessKeySecret, 2);
    }

    @Override
    public ClusterServPlugin setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    @Override
    public void initService() throws Exception {
        String ipAddr = this.calcCIDRIpAddr();
        if (ipAddr == null) {
            throw new Exception("没有可用的IP用于创建集群!");
        }
        this.paramets.put("CIDR_Addr", ipAddr);
    }

    @Override
    public void execService() throws Exception {
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_TKE_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            TkeClient client = new TkeClient(this.cred, this.paramets.get("region").toString(), clientProfile);

            CreateClusterRequest createClusterRequest = new CreateClusterRequest();
            RunInstancesForNode[] runInstancesForNodes1 = new RunInstancesForNode[1];
            RunInstancesForNode runInstancesForNode1 = new RunInstancesForNode();
            runInstancesForNode1.setNodeRole("WORKER");
            // 存放CVM的JSON
            List<String> tkeCvmInfo = (List<String>) this.paramets.get("cvmResults");
            String[] runInstancesPara1 = tkeCvmInfo.toArray(new String[tkeCvmInfo.size()]);
            runInstancesForNode1.setRunInstancesPara(runInstancesPara1);
            runInstancesForNodes1[0] = runInstancesForNode1;
            createClusterRequest.setRunInstancesForNode(runInstancesForNodes1);

            ClusterCIDRSettings clusterCIDRSettings1 = new ClusterCIDRSettings();
            clusterCIDRSettings1.setClusterCIDR(this.paramets.get("CIDR_Addr").toString());
            clusterCIDRSettings1.setMaxNodePodNum(256L);
            clusterCIDRSettings1.setMaxClusterServiceNum(1024L);
            createClusterRequest.setClusterCIDRSettings(clusterCIDRSettings1);

            ClusterBasicSettings clusterBasicSettings1 = new ClusterBasicSettings();
            clusterBasicSettings1.setClusterOs("centos7.2x86_64");
            clusterBasicSettings1.setClusterVersion("1.20.6");
            clusterBasicSettings1.setClusterName(this.paramets.get("clusterName").toString());
            clusterBasicSettings1.setClusterDescription("海豚工程-SaaS构建的业务集群");
            clusterBasicSettings1.setVpcId(this.paramets.get("vpcId").toString());
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
            this.clusterId = createClusterResponse.getClusterId();
        } catch (TencentCloudSDKException e) {
            log.error("createTkeCluster:" + e.getMessage());
            throw new TencentCloudSDKException(e.getMessage());
        }

    }

    @Override
    public void finishService() throws Exception {

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
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_TKE_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            TkeClient client = new TkeClient(this.cred, reGion, clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeClustersRequest describeClustersRequest = new DescribeClustersRequest();
            DescribeClustersResponse describeClustersResponse = client.DescribeClusters(describeClustersRequest);
            if (describeClustersResponse.getClusters().length > 0) {
                for (Cluster cluster : describeClustersResponse.getClusters()) {
                    clusterIds.add(cluster.getClusterId());
                }
            }
        } catch (TencentCloudSDKException e) {
            throw new TencentCloudSDKException(e.getMessage());
        }
        return clusterIds;
    }

    @Override
    public void run() throws Exception {
        this.initService();
        this.execService();
    }

    @Override
    public ClusterServPlugin runner() throws Exception {
        this.run();
        return this;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        Map<String, Object> results = new HashMap<>();
        results.put("clusterId", this.clusterId);
        return results;
    }

    /**
     * 获取有用的网段
     *
     * @return 网段地址
     */
    public String calcCIDRIpAddr() {
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
                HttpProfile httpProfile = new HttpProfile();
                httpProfile.setEndpoint(this.getTENCENT_TKE_URL());
                ClientProfile clientProfile = new ClientProfile();
                clientProfile.setHttpProfile(httpProfile);
                TkeClient client = new TkeClient(this.cred, this.paramets.get("region").toString(), clientProfile);
                DescribeRouteTableConflictsRequest req = new DescribeRouteTableConflictsRequest();
                req.setRouteTableCidrBlock(s);
                req.setVpcId(this.paramets.get("vpcId").toString());
                DescribeRouteTableConflictsResponse resp = client.DescribeRouteTableConflicts(req);
                // 输出json格式的字符串回包
                if (!resp.getHasConflict()) {
                    return s;
                }
            } catch (TencentCloudSDKException e) {
                log.error("[腾讯云SDK][集群网段检测]异常信息: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 获取集群的状态
     *
     * @return
     */
    public Boolean clusterStatus(String clusterId, String region) throws Exception {
        Boolean ClusterStatus = false;
        try {
            /*
             * 获取集群状态
             */
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_TKE_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            clientProfile.setLanguage(Language.ZH_CN);
            TkeClient client = new TkeClient(this.cred, region, clientProfile);
            DescribeClustersRequest describeClustersRequest = new DescribeClustersRequest();
            String[] clusterIds1 = {clusterId};
            describeClustersRequest.setClusterIds(clusterIds1);
            DescribeClustersResponse describeClustersResponse = client.DescribeClusters(describeClustersRequest);
            for (Cluster cluster : describeClustersResponse.getClusters()) {
                ClusterStatus = cluster.getClusterStatus().equals("Running");
            }
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云SDK][获取集群的状态]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return ClusterStatus;
    }

    /**
     * 获取集群下所有CVM节点的实例id
     *
     * @param clusterId 集群id
     * @return
     */
    public ArrayList<String> clusterServerNodeInstanceId(String clusterId, String region) throws Exception {
        ArrayList<String> results = new ArrayList<>();
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_TKE_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            TkeClient client = new TkeClient(this.cred, region, clientProfile);
            DescribeExistedInstancesRequest describeExistedInstancesRequest = new DescribeExistedInstancesRequest();
            describeExistedInstancesRequest.setClusterId(clusterId);
            DescribeExistedInstancesResponse describeExistedInstancesResponse = client.DescribeExistedInstances(describeExistedInstancesRequest);
            if (describeExistedInstancesResponse.getTotalCount() < 1) {
                throw new Exception("当前没有节点!");
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
            log.error("[腾讯云SDK][获取集群Node节点实例ID]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 获取CVM实例ID组的状态是否全部都正常
     *
     * @param instanceIds
     * @return
     */
    public Boolean checkInstanceListStatus(ArrayList<String> instanceIds, String region) throws Exception {
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_CVM_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            CvmClient client = new CvmClient(this.cred, region, clientProfile);
            DescribeInstancesStatusRequest describeInstancesStatusRequest = new DescribeInstancesStatusRequest();
            int size = instanceIds.size();
            String[] instanceIds1 = instanceIds.toArray(new String[size]);
            describeInstancesStatusRequest.setInstanceIds(instanceIds1);
            DescribeInstancesStatusResponse describeInstancesStatusResponse = client.DescribeInstancesStatus(describeInstancesStatusRequest);

            InstanceStatus[] instanceStatusSet = describeInstancesStatusResponse.getInstanceStatusSet();
            if (instanceStatusSet.length > 0) {
                for (InstanceStatus instanceStatus : instanceStatusSet) {
                    if (!instanceStatus.getInstanceState().equals("RUNNING")) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            log.error("[腾讯云SDK][获取集群Node节点状态]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return true;
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
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_TKE_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            TkeClient client = new TkeClient(this.cred, reGion, clientProfile);
            DescribeClusterSecurityRequest describeClusterSecurityRequest = new DescribeClusterSecurityRequest();
            describeClusterSecurityRequest.setClusterId(clusterId);
            DescribeClusterSecurityResponse describeClusterSecurityResponse = client
                    .DescribeClusterSecurity(describeClusterSecurityRequest);
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
            log.error("[腾讯云SDK][获取集群的密钥信息]异常信息: {}", e.getMessage());
            throw new TencentCloudSDKException(e.getMessage());
        }
        return results;
    }


    /**
     * 检查集群状态
     *
     * @param clusterId
     * @return
     */
    public Boolean checkClusterStatus(String clusterId, String region) throws Exception {
        Boolean ClusterAllNodeStatus = false;
        try {
            // 判断集群状态，如果在运行中
            if (this.clusterStatus(clusterId, region)) {
                // 检查节点是否都已经可以了
                if (this.checkInstanceListStatus(this.clusterServerNodeInstanceId(clusterId, region), region)) {
                    ClusterAllNodeStatus = true;
                }
            }
        } catch (Exception e) {
            log.error("[腾讯云SDK][检查集群状态]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return ClusterAllNodeStatus;
    }

    /**
     * 开启外网访问
     * @param paramets
     * @throws Exception
     */
    public void OpenNetWork(Map<String, Object> paramets) throws Exception {
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_TKE_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            clientProfile.setLanguage(Language.ZH_CN);
            TkeClient client = new TkeClient(this.cred, paramets.get("region").toString(), clientProfile);

            // 查询外网是否开放
            DescribeClusterEndpointStatusRequest describeClusterEndpointStatusRequest = new DescribeClusterEndpointStatusRequest();
            describeClusterEndpointStatusRequest.setClusterId(paramets.get("clusterId").toString());
            describeClusterEndpointStatusRequest.setIsExtranet(true);
            // 返回的resp是一个DescribeClusterEndpointStatusResponse的实例，与请求对象对应
            DescribeClusterEndpointStatusResponse describeClusterEndpointStatusResponse = client.DescribeClusterEndpointStatus(describeClusterEndpointStatusRequest);
            if (!describeClusterEndpointStatusResponse.getStatus().equals("Created")){
                // 判断是否是开启中
                if (!describeClusterEndpointStatusResponse.getStatus().equals("Creating")){
                    /*
                     * 创建外联网端点
                     */
                    CreateClusterEndpointVipRequest createClusterEndpointVipRequest = new CreateClusterEndpointVipRequest();
                    createClusterEndpointVipRequest.setClusterId(paramets.get("clusterId").toString());
                    String[] securityPolicies1 = {"0.0.0.0/0"};
                    createClusterEndpointVipRequest.setSecurityPolicies(securityPolicies1);
                    client.CreateClusterEndpointVip(createClusterEndpointVipRequest);
                }
            }
        }catch (Exception e){
            log.error("[腾讯云SDK][开启集群外网访问]异常信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 获取集群节点的IP
     * @param reGion
     * @param clusterId
     * @return
     * @throws TencentCloudSDKException
     */
    public ArrayList<String> clusterServerIp(String reGion, String clusterId) throws TencentCloudSDKException {
        ArrayList<String> results = new ArrayList<>();
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_TKE_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            TkeClient client = new TkeClient(this.cred, reGion, clientProfile);

            DescribeExistedInstancesRequest describeExistedInstancesRequest = new DescribeExistedInstancesRequest();
            describeExistedInstancesRequest.setClusterId(clusterId);
            DescribeExistedInstancesResponse describeExistedInstancesResponse = client.DescribeExistedInstances(describeExistedInstancesRequest);
            if (describeExistedInstancesResponse.getTotalCount() < 1) {
                throw new TencentCloudSDKException("当前集群异常,没有节点!");
            }
            ExistedInstance[] existedInstances = describeExistedInstancesResponse.getExistedInstanceSet();
            for (ExistedInstance existedInstance : existedInstances) {
                if (existedInstance.getAlreadyInCluster() != null) {
                    if (Objects.equals(existedInstance.getAlreadyInCluster(), clusterId)) {
                        if (existedInstance.getPublicIpAddresses().length > 0) {
                            results.add(existedInstance.getPublicIpAddresses()[0]);
                        }
                    }
                }
            }
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云SDK][获取集群节点IP]异常信息: {}", e.getMessage());
            throw new TencentCloudSDKException(e.getMessage());
        }
        return results;
    }

    /**
     * CVM数据计算
     *
     * @param reGion      归属
     * @param Zone        可用区
     * @param Concurrency 并发
     * @param buyMode     0：按需，1：预付
     * @return 计算完的数据
     * @throws Exception 抛出异常
     */
    public Map<String, Object> calculateAssets(String reGion, String Zone, Integer Concurrency, Integer buyMode) throws Exception {
        ArrayList<Map<String, Object>> assetList = new ArrayList<>();
        try {
            HttpProfile httpProfile = new HttpProfile();
            // 获取归属的可用区下可以使用的CVM规格
            CvmServ cvmServ = new CvmServ(this.cred, httpProfile);
            // 获取cvm的数据
            Cvm cvm = new Cvm();
            cvm.setReGion(reGion);
            cvm.setZone(Zone);

            Map<String, Object> calculateAssetsResults = new HashMap<>();
            double totalPrice = 0F;
            Map<String, Object> cvmData = cvmServ.calculatePriceForCvm(Concurrency, buyMode, cvm);

            for (Map<String, Object> cvmItems: (ArrayList<Map<String, Object>>)cvmData.get("cvmLists")){
                Map<String, Object> cvmMap = new HashMap<>();
                if (buyMode == 0){
                    cvmMap.put("price", new BigDecimal(Float.valueOf(cvmItems.get("price").toString()) * 24)
                            .setScale(2, RoundingMode.HALF_UP).floatValue() + "元/天");
                    totalPrice = totalPrice + Double.valueOf(cvmItems.get("price").toString()) * 24;
                }else{
                    cvmMap.put("price", new BigDecimal(Float.valueOf(cvmItems.get("price").toString()))
                            .setScale(2, RoundingMode.HALF_UP).floatValue() + "元/月");
                    totalPrice = totalPrice + Double.valueOf(cvmItems.get("price").toString());
                }

                cvmMap.put("info", cvmItems.get("cvm"));
                cvmMap.put("spec", "云服务器");
                cvmMap.put("remark", cvmItems.get("remark"));
                cvmMap.put("type", "CVM");
                assetList.add(cvmMap);
            }

            // 组装数据
            switch (Concurrency){
                case 1:
                    calculateAssetsResults.put("current", "约100-200人同时在线");
                    break;
                case 50:
                    calculateAssetsResults.put("current", "约500-1000人同时在线");
                    break;
                case 250:
                    calculateAssetsResults.put("current", "约2500~5000人同时在线");
                    break;
            }
            // 组装数据
            String datetime = "";
            try {
                SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                datetime = simpleDateFormat2.format(new Date());
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
            calculateAssetsResults.put("createtime", datetime);
            calculateAssetsResults.put("cloud", "腾讯云");

            // cfs计费
            Map<String, Object> cfsMap = new HashMap<>();
            cfsMap.put("price", 58 + "元/月");
            cfsMap.put("spec", "通用标准型存储CFS");
            cfsMap.put("info", "标准版");
            cfsMap.put("remark", "区间在0.33至0.58元/GiB/月，按照100GiB/月做预算，以腾讯云实际扣费为准，这里预算会冗余到0.58元/GiB/月。不建议预付费，海豚创建以「后付费」模式为准，节省成本。");
            cfsMap.put("type", "CFS");
            totalPrice = totalPrice + 58;
            assetList.add(cfsMap);

            // 网络计费
            Map<String, Object> netWorkMap = new HashMap<>();
            netWorkMap.put("price", 72.16 + "元/0.5个月");
            netWorkMap.put("spec", "通用网络流量");
            netWorkMap.put("info", "标准版");
            netWorkMap.put("remark", "区间在0.8元/GiB，带宽为200/Mbps，预算按照每天6/GiB，15天预计花72/元，按照15天做预算，以腾讯云实际扣费为准。不建议预付费，海豚创建以「后付费」模式为准，节省成本。");
            netWorkMap.put("type", "NETWORK");
            totalPrice = totalPrice + 72.16;
            assetList.add(netWorkMap);

            calculateAssetsResults.put("totalPrice", totalPrice);
            calculateAssetsResults.put("assetList", assetList);
            return calculateAssetsResults;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
