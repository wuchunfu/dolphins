package com.dolphin.saas.commons.clouds.tencent;

import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.service.ClusterService;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.profile.Language;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.*;
import com.tencentcloudapi.vpc.v20170312.VpcClient;
import com.tencentcloudapi.vpc.v20170312.models.*;
import com.tencentcloudapi.vpc.v20170312.models.Filter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
public class VpcServPlugin extends MasterServ implements Frame {

    private final String VpcName = "海豚工程-专用VPC";
    private final Map<String, Object> results = new HashMap<>();

    /**
     * 加载厂商
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     */
    public VpcServPlugin(String AccessKeyId, String AccessKeySecret) {
        super(AccessKeyId, AccessKeySecret, 2);
    }

    @Override
    public Frame setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    @Override
    public void initService() throws Exception {
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(this.getTENCENT_VPC_URL());
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setLanguage(Language.ZH_CN);
        clientProfile.setHttpProfile(httpProfile);
        VpcClient client = new VpcClient(this.cred, this.paramets.get("region").toString(), clientProfile);
        // 先查有没有这个vpc网络，有就不新建了
        DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
        describeVpcsRequest.setOffset("0");
        describeVpcsRequest.setLimit("100");
        DescribeVpcsResponse describeVpcsResponse = client.DescribeVpcs(describeVpcsRequest);
        Vpc[] vpcLists = describeVpcsResponse.getVpcSet();

        // 判断是否已经有VPC了
        // 如果有，就查有没有海豚相关的VPC
        // 如果有，就优先用已经有了的，不让客户重复创建
        if (vpcLists.length > 0) {
            for (Vpc vpcList : vpcLists) {
                if (vpcList.getVpcName().equals(VpcName)) {
                    this.results.put("vpcId", vpcList.getVpcId());
                    this.results.put("ipRange", vpcList.getCidrBlock());
                }
            }
        }

        // 查询子网是否存在
        DescribeSubnetsRequest describeSubnetsRequest = new DescribeSubnetsRequest();
        Filter[] filters1 = new Filter[1];
        Filter filter1 = new Filter();
        filter1.setName("zone");
        String[] values1 = {this.paramets.get("zone").toString()};
        filter1.setValues(values1);
        filters1[0] = filter1;
        describeSubnetsRequest.setFilters(filters1);
        DescribeSubnetsResponse describeSubnetsResponse = client.DescribeSubnets(describeSubnetsRequest);
        Subnet[] subnets = describeSubnetsResponse.getSubnetSet();
        for (Subnet subnet : subnets) {
            if (subnet.getVpcId().equals(this.results.get("vpcId").toString())) {
                this.results.put("subNetId", subnet.getSubnetId());
            }
        }
    }

    @Override
    public void execService() throws Exception {
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(this.getTENCENT_VPC_URL());
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setLanguage(Language.ZH_CN);
        clientProfile.setHttpProfile(httpProfile);
        VpcClient client = new VpcClient(this.cred, this.paramets.get("region").toString(), clientProfile);
        // 如果没有可用的VPC返回，就直接创建
        if (!this.results.containsKey("vpcId")) {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateVpcRequest createVpcRequest = new CreateVpcRequest();
            createVpcRequest.setVpcName(VpcName);
            createVpcRequest.setCidrBlock(this.paramets.get("subnet").toString());
            CreateVpcResponse createVpcResponse = client.CreateVpc(createVpcRequest);
            this.results.put("vpcId", createVpcResponse.getVpc().getVpcId());
            this.results.put("ipRange", createVpcResponse.getVpc().getCidrBlock());
        }

        if (!this.results.containsKey("subNetId")) {
            // 获取所有的IP列表/24位
            ArrayList<String> ipGroups = new ArrayList<>();
            for (int i=1; i<=254; i++){
                ipGroups.add(String.format("10.56.%s.0/24", i));
            }

            // 如果该区域没有子网，则计算下，哪个子网可以创建
            DescribeSubnetsRequest describeSubnetsRequest = new DescribeSubnetsRequest();
            DescribeSubnetsResponse describeSubnetsResponse = client.DescribeSubnets(describeSubnetsRequest);
            Subnet[] subnets = describeSubnetsResponse.getSubnetSet();
            for (Subnet subnet: subnets){
                ipGroups.remove(subnet.getCidrBlock());
            }

            // 创建子网
            CreateSubnetRequest createSubnetRequest = new CreateSubnetRequest();
            createSubnetRequest.setVpcId(this.results.get("vpcId").toString());
            createSubnetRequest.setSubnetName("海豚工程-私有子网");
            createSubnetRequest.setCidrBlock(ipGroups.get(new Random().nextInt(ipGroups.size())));
            createSubnetRequest.setZone(this.paramets.get("zone").toString());
            CreateSubnetResponse createSubnetResponse = client.CreateSubnet(createSubnetRequest);
            this.results.put("subNetId", createSubnetResponse.getSubnet().getSubnetId());
        }
    }

    @Override
    public void finishService() throws Exception {

    }

    @Override
    public void run() throws Exception {

    }

    @Override
    public Frame runner() throws Exception {
        ClusterService clusterService = (ClusterService) this.paramets.get("clusterService");
        Long cid = Long.parseLong(this.paramets.get("cid").toString());
        RedisCommonUtils redisCommonUtils = (RedisCommonUtils) this.paramets.get("redisCommonUtils");

        if (!redisCommonUtils.hasKeys("VpcServPlugin." + cid)) {
            redisCommonUtils.noExpireSset("VpcServPlugin." + cid, 1);
            try {
                clusterService.UpdateStage(3, cid, 1);
                this.initService();
                this.execService();
                clusterService.UpdateStage(3, cid, 2);
            } catch (Exception e) {
                clusterService.UpdateStage(3, cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.noExpireSset("VpcServPlugin." + cid, 2);
            }
        }
        return this;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        return this.results;
    }

    /**
     * 获取归属的列表
     * @return
     * @throws Exception
     */
    public ArrayList<Map<String, Object>> regionsLists() throws Exception {
        ArrayList<Map<String, Object>> regionArr = new ArrayList<>();
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_CVM_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            clientProfile.setLanguage(Language.ZH_CN);
            CvmClient client = new CvmClient(this.cred, "", clientProfile);
            DescribeRegionsRequest describeRegionsRequest = new DescribeRegionsRequest();
            DescribeRegionsResponse describeRegionsResponse = client.DescribeRegions(describeRegionsRequest);
            RegionInfo[] regionInfos = describeRegionsResponse.getRegionSet();
            if (regionInfos.length < 1) {
                throw new Exception("获取地域列表失败!");
            }

            for (RegionInfo regionInfo : regionInfos) {
                Map<String, Object> obj = new HashMap<>();
                obj.put("label", regionInfo.getRegionName());
                obj.put("value", regionInfo.getRegion());
                regionArr.add(obj);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return regionArr;
    }

    /**
     * 获取可用区列表
     * @param regionsId 归属地域
     * @return
     * @throws Exception
     */
    public ArrayList<Map<String, Object>> zoneLists(String regionsId) throws Exception {
        ArrayList<Map<String, Object>> zoneArr = new ArrayList<>();
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_CVM_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            clientProfile.setLanguage(Language.ZH_CN);
            CvmClient client = new CvmClient(this.cred, regionsId, clientProfile);
            DescribeZonesRequest describeZonesRequest = new DescribeZonesRequest();
            DescribeZonesResponse describeZonesResponse = client.DescribeZones(describeZonesRequest);
            ZoneInfo[] zoneInfos = describeZonesResponse.getZoneSet();
            if (zoneInfos.length < 1) {
                throw new TencentCloudSDKException("获取可用区失败!");
            }

            for (ZoneInfo zoneInfo : zoneInfos) {
                if (zoneInfo.getZoneState().equals("AVAILABLE")) {
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("label", zoneInfo.getZoneName());
                    obj.put("value", zoneInfo.getZone());
                    zoneArr.add(obj);
                }
            }
        } catch (TencentCloudSDKException e) {
            throw new Exception(e.getMessage());
        }
        return zoneArr;
    }
}
