package com.dolphin.saas.commons.clouds.tencent;

import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.service.ClusterService;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.profile.Language;
import com.tencentcloudapi.vpc.v20170312.VpcClient;
import com.tencentcloudapi.vpc.v20170312.models.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SecGroupServPlugin extends MasterServ implements Frame {

    private final Boolean createStatus = false;
    private String GroupId = "";

    /**
     * 加载厂商
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     */
    public SecGroupServPlugin(String AccessKeyId, String AccessKeySecret) {
        super(AccessKeyId, AccessKeySecret, 2);
    }

    @Override
    public Frame setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    @Override
    public void initService() throws TencentCloudSDKException {
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_VPC_URL());
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setLanguage(Language.ZH_CN);
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            VpcClient client = new VpcClient(this.cred, this.paramets.get("region").toString(), clientProfile);

            DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
            DescribeSecurityGroupsResponse describeSecurityGroupsResponse = client.DescribeSecurityGroups(describeSecurityGroupsRequest);

            SecurityGroup[] securityGroups = describeSecurityGroupsResponse.getSecurityGroupSet();
            if (securityGroups.length > 0) {
                for (SecurityGroup securityGroup : securityGroups) {
                    if (securityGroup.getSecurityGroupName().equals("海豚工程-专用安全组")) {
                        this.GroupId = securityGroup.getSecurityGroupId();
                    }
                }
            }
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云SDK][安全组检查]失败: {}", e.getMessage());
            throw new TencentCloudSDKException(e.getMessage());
        }
    }

    @Override
    public void execService() throws Exception {
        try {
            if (this.GroupId.equals("")) {
                HttpProfile httpProfile = new HttpProfile();
                httpProfile.setEndpoint(this.getTENCENT_VPC_URL());
                ClientProfile clientProfile = new ClientProfile();
                clientProfile.setLanguage(Language.ZH_CN);
                clientProfile.setHttpProfile(httpProfile);
                VpcClient client = new VpcClient(this.cred, this.paramets.get("region").toString(), clientProfile);

                CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest();
                createSecurityGroupRequest.setGroupName("海豚工程-专用安全组");
                createSecurityGroupRequest.setGroupDescription("海豚工程专用");
                // 返回的resp是一个CreateSecurityGroupResponse的实例，与请求对象对应
                CreateSecurityGroupResponse createSecurityGroupResponse = client.CreateSecurityGroup(createSecurityGroupRequest);

                this.GroupId = createSecurityGroupResponse.getSecurityGroup().getSecurityGroupId();
                this.createSecurityGroupPolicies(this.GroupId, this.paramets.get("region").toString());
            }
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云SDK][安全组创建]失败: {}", e.getMessage());
            throw new TencentCloudSDKException(e.getMessage());
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

        if (!redisCommonUtils.hasKeys("SecGroupServPlugin." + cid)) {
            redisCommonUtils.noExpireSset("SecGroupServPlugin." + cid, 1);
            try {
                clusterService.UpdateStage(2, cid, 1);
                this.initService();
                this.execService();
                clusterService.UpdateStage(2, cid, 2);
            } catch (Exception e) {
                clusterService.UpdateStage(2, cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.noExpireSset("SecGroupServPlugin." + cid, 2);
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
        Map<String, Object> results = new HashMap<>();
        results.put("groupId", this.GroupId);
        return results;
    }

    /**
     * 创建安全组规则
     *
     * @param groupId 安全组名
     * @return
     */
    public void createSecurityGroupPolicies(String groupId, String reGion) throws Exception {
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_VPC_URL());
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setLanguage(Language.ZH_CN);
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            VpcClient client = new VpcClient(this.cred, reGion, clientProfile);
            /*
             * 创建入站规则
             */
            CreateSecurityGroupPoliciesRequest req = new CreateSecurityGroupPoliciesRequest();
            req.setSecurityGroupId(groupId);
            SecurityGroupPolicySet securityGroupPolicySet1 = new SecurityGroupPolicySet();
            SecurityGroupPolicy[] securityGroupPolicys1 = new SecurityGroupPolicy[4];
            SecurityGroupPolicy securityGroupPolicy1 = new SecurityGroupPolicy();
            securityGroupPolicy1.setPolicyIndex(0L);
            securityGroupPolicy1.setProtocol("ICMP");
            securityGroupPolicy1.setAction("ACCEPT");
            securityGroupPolicy1.setPolicyDescription("支持Ping服务");
            securityGroupPolicys1[0] = securityGroupPolicy1;
            SecurityGroupPolicy securityGroupPolicy2 = new SecurityGroupPolicy();
            securityGroupPolicy2.setPolicyIndex(0L);
            securityGroupPolicy2.setProtocol("TCP");
            securityGroupPolicy2.setPort("80");
            securityGroupPolicy2.setAction("ACCEPT");
            securityGroupPolicy2.setPolicyDescription("放通Web服务HTTP（80），如 Apache、Nginx");
            securityGroupPolicys1[1] = securityGroupPolicy2;
            SecurityGroupPolicy securityGroupPolicy3 = new SecurityGroupPolicy();
            securityGroupPolicy3.setPolicyIndex(0L);
            securityGroupPolicy3.setProtocol("TCP");
            securityGroupPolicy3.setPort("443");
            securityGroupPolicy3.setAction("ACCEPT");
            securityGroupPolicy3.setPolicyDescription("放通Web服务HTTPS（443），如 Apache、Nginx");
            securityGroupPolicys1[2] = securityGroupPolicy3;

            SecurityGroupPolicy securityGroupPolicy4 = new SecurityGroupPolicy();
            securityGroupPolicy4.setPolicyIndex(0L);
            securityGroupPolicy4.setProtocol("TCP");
            securityGroupPolicy4.setPort("22");
            securityGroupPolicy4.setAction("ACCEPT");
            securityGroupPolicy4.setPolicyDescription("放通SSH服务");
            securityGroupPolicys1[3] = securityGroupPolicy4;

            securityGroupPolicySet1.setIngress(securityGroupPolicys1);
            req.setSecurityGroupPolicySet(securityGroupPolicySet1);
            client.CreateSecurityGroupPolicies(req);

            /*
             * 创建出站规则
             */
            CreateSecurityGroupPoliciesRequest req2 = new CreateSecurityGroupPoliciesRequest();
            req2.setSecurityGroupId(groupId);
            SecurityGroupPolicySet securityGroupPolicySet2 = new SecurityGroupPolicySet();
            SecurityGroupPolicy[] securityGroupPolicys2 = new SecurityGroupPolicy[1];
            SecurityGroupPolicy securityGroupPolicy5 = new SecurityGroupPolicy();
            securityGroupPolicy5.setPolicyIndex(0L);
            securityGroupPolicy5.setProtocol("all");
            securityGroupPolicy5.setAction("ACCEPT");
            securityGroupPolicy5.setPolicyDescription("全部放行");
            securityGroupPolicys2[0] = securityGroupPolicy5;
            securityGroupPolicySet2.setEgress(securityGroupPolicys2);
            req2.setSecurityGroupPolicySet(securityGroupPolicySet2);
            client.CreateSecurityGroupPolicies(req2);
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云SDK][安全组策略创建]失败信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 获取所有的规则
     * @param securityGroupId
     * @param reGion
     * @return
     * @throws Exception
     */
    public ArrayList<Map<String, String>> getEgressLists(String securityGroupId, String reGion) throws Exception {
        ArrayList<Map<String, String>> results = new ArrayList<>();
        try{
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_VPC_URL());
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setLanguage(Language.ZH_CN);
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            VpcClient client = new VpcClient(this.cred, reGion, clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeSecurityGroupPoliciesRequest req = new DescribeSecurityGroupPoliciesRequest();
            req.setSecurityGroupId(securityGroupId);
            // 返回的resp是一个DescribeSecurityGroupPoliciesResponse的实例，与请求对象对应
            DescribeSecurityGroupPoliciesResponse resp = client.DescribeSecurityGroupPolicies(req);
            // 输出json格式的字符串回包
            for (SecurityGroupPolicy egressItems: resp.getSecurityGroupPolicySet().getEgress()){
                Map<String, String> items = new HashMap<>();
                items.put("action", egressItems.getAction());
                items.put("ips", egressItems.getCidrBlock());
                items.put("modifyTime", egressItems.getModifyTime());
                items.put("desc", egressItems.getPolicyDescription());
                items.put("port", egressItems.getPort());
                items.put("protocol", egressItems.getProtocol());
                items.put("tag", "out");
                results.add(items);
            }
            for (SecurityGroupPolicy egressItems: resp.getSecurityGroupPolicySet().getIngress()){
                Map<String, String> items = new HashMap<>();
                items.put("action", egressItems.getAction());
                items.put("ips", egressItems.getCidrBlock());
                items.put("modifyTime", egressItems.getModifyTime());
                items.put("desc", egressItems.getPolicyDescription());
                items.put("port", egressItems.getPort());
                items.put("protocol", egressItems.getProtocol());
                items.put("tag", "in");
                results.add(items);
            }
        } catch (TencentCloudSDKException e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }
}
