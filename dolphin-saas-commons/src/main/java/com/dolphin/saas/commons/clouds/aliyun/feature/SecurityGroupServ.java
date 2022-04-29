package com.dolphin.saas.commons.clouds.aliyun.feature;

import com.aliyun.ecs20140526.models.*;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.service.ClusterService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SecurityGroupServ extends MasterServ implements Frame {
    private final com.aliyun.ecs20140526.Client client;
    private final Map<String, Object> results = new HashMap<>();

    /**
     * 加载厂商
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     */
    public SecurityGroupServ(String AccessKeyId, String AccessKeySecret, Map<String, Object> paramets) throws Exception {
        super(AccessKeyId, AccessKeySecret, 1);
        // 要提前加载
        this.paramets = paramets;

        // 访问的域名
        // 访问的域名
        this.config.setEndpoint(
                this.getALIYUN_ECS_URL()
                        .replace("{REGIONS}", this.paramets.get("region").toString())
        );
        this.client = new com.aliyun.ecs20140526.Client(config);
    }

    /**
     * 获取GROUPID
     * @param SecurityGroupName
     * @return
     * @throws Exception
     */
    public String aliveSecurityGroupId(String SecurityGroupName) throws Exception {
        String groupId = null;
        try {
            DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest()
                    .setRegionId(this.paramets.get("region").toString())
                    .setSecurityGroupName(SecurityGroupName);
            List<DescribeSecurityGroupsResponseBody.DescribeSecurityGroupsResponseBodySecurityGroupsSecurityGroup> groupLists = this.client.describeSecurityGroups(describeSecurityGroupsRequest).getBody().getSecurityGroups().getSecurityGroup();
            if (groupLists.size() > 0){
                groupId = groupLists.get(0).getSecurityGroupId();
            }
        }catch (Exception e){
            log.error("[阿里云SDK][安全组策略]获取安全组异常，信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return groupId;
    }

    @Override
    public SecurityGroupServ setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    @Override
    public void initService() throws Exception {

    }

    @Override
    public void execService() throws Exception {
        String groupId = this.aliveSecurityGroupId("海豚工程安全组");

        if (groupId != null){
            this.results.put("securityGroupId", groupId);
        }else{
            // 创建安全组
            try {
                CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest()
                        .setRegionId(this.paramets.get("region").toString())
                        .setDescription("海豚工程DevOPS安全组")
                        .setSecurityGroupName("海豚工程安全组")
                        .setVpcId(this.paramets.get("vpcId").toString())
                        .setSecurityGroupType("normal");
                this.results.put("securityGroupId", this.client.createSecurityGroup(createSecurityGroupRequest).getBody().getSecurityGroupId());
            }catch (Exception e){
                throw new Exception(e.getMessage());
            }

            // 根据安全组加安全策略(进策略)
            try {
                AuthorizeSecurityGroupRequest authorizeSecurityGroupRequest = new AuthorizeSecurityGroupRequest()
                        .setRegionId(this.paramets.get("region").toString())
                        .setSecurityGroupId(this.results.get("securityGroupId").toString())
                        .setIpProtocol("all")
                        .setPortRange("-1/-1")
                        .setSourceCidrIp("0.0.0.0/0")
                        .setDescription("所有允许");
                this.client.authorizeSecurityGroup(authorizeSecurityGroupRequest);
            }catch (Exception e){
                log.error("[阿里云SDK][安全组策略]进口策略增加异常，信息: {}", e.getMessage());
                throw new Exception(e.getMessage());
            }

            // 根据安全组加安全策略(出策略)
            try {
                AuthorizeSecurityGroupEgressRequest authorizeSecurityGroupEgressRequest = new AuthorizeSecurityGroupEgressRequest()
                        .setRegionId(this.paramets.get("region").toString())
                        .setSecurityGroupId(this.results.get("securityGroupId").toString())
                        .setIpProtocol("all")
                        .setPortRange("-1/-1")
                        .setDescription("所有放行")
                        .setDestCidrIp("0.0.0.0/0");
                // 复制代码运行请自行打印 API 的返回值
                this.client.authorizeSecurityGroupEgress(authorizeSecurityGroupEgressRequest);
            }catch (Exception e) {
                log.error("[阿里云SDK][安全组策略]出口策略增加异常，信息: {}", e.getMessage());
                throw new Exception(e.getMessage());
            }
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

        if (!redisCommonUtils.hasKeys("SecurityServ." + cid)){
            redisCommonUtils.noExpireSset("SecurityServ." + cid, 1);
            try {
                clusterService.UpdateStage(4, cid, 1);
                this.execService();
                clusterService.UpdateStage(4, cid, 2);
            } catch (Exception e) {
                clusterService.UpdateStage(4, cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.getRedisTemplate().delete("SecurityServ." + cid);
            }
        }
    }

    @Override
    public SecurityGroupServ runner() throws Exception {
        this.run();
        return this;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        return this.results;
    }

    /**
     * 获取所有安全组策略
     * @param securityGroupId
     * @return
     * @throws Exception
     */
    public ArrayList<Map<String, String>> getEgressLists(String securityGroupId) throws Exception {
        ArrayList<Map<String, String>> results = new ArrayList<>();
        try {
            DescribeSecurityGroupAttributeRequest describeSecurityGroupAttributeRequest = new DescribeSecurityGroupAttributeRequest()
                    .setSecurityGroupId(securityGroupId)
                    .setRegionId(this.paramets.get("region").toString());
            DescribeSecurityGroupAttributeResponse describeSecurityGroupAttributeResponse = client.describeSecurityGroupAttribute(describeSecurityGroupAttributeRequest);

            List<DescribeSecurityGroupAttributeResponseBody.DescribeSecurityGroupAttributeResponseBodyPermissionsPermission> descLists = describeSecurityGroupAttributeResponse.getBody().getPermissions().getPermission();

            for (DescribeSecurityGroupAttributeResponseBody.DescribeSecurityGroupAttributeResponseBodyPermissionsPermission permissionItems:descLists){
                Map<String, String> items = new HashMap<>();
                items.put("action", permissionItems.getPolicy());
                items.put("ips", permissionItems.getSourceCidrIp());
                items.put("modifyTime", permissionItems.getCreateTime());
                items.put("desc", permissionItems.getDescription());
                items.put("port", permissionItems.getPortRange());
                items.put("protocol", permissionItems.getIpProtocol());
                items.put("tag", "out");
                results.add(items);
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return results;
    }
}
