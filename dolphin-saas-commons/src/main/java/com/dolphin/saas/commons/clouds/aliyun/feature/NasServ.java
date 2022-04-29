package com.dolphin.saas.commons.clouds.aliyun.feature;

import com.alibaba.fastjson.JSON;
import com.aliyun.nas20170626.models.*;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.service.ClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class NasServ extends MasterServ implements Frame {

    @Autowired
    private RedisCommonUtils redisCommonUtils;

    private final com.aliyun.nas20170626.Client client;

    public NasServ(String AccessKeyId, String AccessKeySecret, Map<String, Object> paramets) throws Exception {
        super(AccessKeyId, AccessKeySecret, 1);
        this.paramets = paramets;

        // 访问的域名
        this.config.setEndpoint(
                this.getALIYUN_NAS_URL().replace("{REGIONS}", this.paramets.get("region").toString())
        );
        this.client = new com.aliyun.nas20170626.Client(this.config);
    }

    @Override
    public Frame setVal(Map<String, Object> paramets) {
        return null;
    }

    @Override
    public void initService() throws Exception {

    }

    @Override
    public void execService() throws Exception {
        try {
            this.client.openNASService();
            log.info("[阿里云SDK][Nas服务开启]开启成功!");
        } catch (Exception e) {
            if (!e.getMessage().matches("(.*)You have already purchased the NAS service. Go to the NAS console to start using it(.*)")) {
                log.error("[阿里云SDK][Nas服务开启]异常信息: {}", e.getMessage());
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

        if (!redisCommonUtils.hasKeys("NasServ." + cid)) {
            redisCommonUtils.noExpireSset("NasServ." + cid, 1);
            try {
                clusterService.UpdateStage(1, cid, 1);
                this.execService();
                clusterService.UpdateStage(1, cid, 2);
            } catch (Exception e) {
                clusterService.UpdateStage(1, cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.getRedisTemplate().delete("NasServ." + cid);
            }
        }
    }

    /**
     * 创建海豚工程的NFS服务
     * @throws Exception
     */
    public Map<String, Object> createNFS() throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            CreateFileSystemRequest createFileSystemRequest = new CreateFileSystemRequest()
                    .setFileSystemType("standard")
                    .setChargeType("PayAsYouGo")
                    .setStorageType("Performance")
                    .setProtocolType("NFS")
                    .setDescription("海豚工程NFS");
            // 复制代码运行请自行打印 API 的返回值
            String fileSystemId = client.createFileSystem(createFileSystemRequest).getBody().getFileSystemId();

            /*
             * 创建挂载点
             */
            CreateMountTargetRequest createMountTargetRequest = new CreateMountTargetRequest()
                .setFileSystemId(fileSystemId)
                .setAccessGroupName("DEFAULT_VPC_GROUP_NAME")
                .setNetworkType("Vpc")
                .setVpcId(this.paramets.get("vpcId").toString())
                .setVSwitchId(this.paramets.get("switchId").toString())
                .setSecurityGroupId(this.paramets.get("securityGroupId").toString());
            results.put("cfsIp", client.createMountTarget(createMountTargetRequest).getBody().getMountTargetDomain());
            results.put("cfsId", fileSystemId);
        }catch (Exception e){
            log.error("[服务部署][Nas服务]创建失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 获取Nas基本信息
     * @return
     * @throws Exception
     */
    public Map<String, Object> getNasAddr() throws Exception {
        Map<String, Object> results = new HashMap<>();

        try {
            DescribeFileSystemsRequest describeFileSystemsRequest = new DescribeFileSystemsRequest()
                    .setVpcId(this.paramets.get("vpcId").toString());
            DescribeFileSystemsResponse describeFileSystemsResponse = this.client.describeFileSystems(describeFileSystemsRequest);
            List<DescribeFileSystemsResponseBody.DescribeFileSystemsResponseBodyFileSystemsFileSystem> describeFileSystemsResponseBodyFileSystemsFileSystemList = describeFileSystemsResponse.getBody().getFileSystems().getFileSystem();

            if (describeFileSystemsResponseBodyFileSystemsFileSystemList.size() > 0){
                for (DescribeFileSystemsResponseBody.DescribeFileSystemsResponseBodyFileSystemsFileSystem describeFileSystemItems: describeFileSystemsResponseBodyFileSystemsFileSystemList) {
                    if (describeFileSystemItems.getDescription().equals("海豚工程NFS") && describeFileSystemItems.getStatus().equals("Running") && describeFileSystemItems.getMountTargets().getMountTarget().size() > 0){
                        results.put("cfsIp", describeFileSystemItems.getMountTargets().getMountTarget().get(0).getMountTargetDomain());
                        results.put("cfsId", describeFileSystemItems.getFileSystemId());
                    }
                }
            }

            if (!results.containsKey("cfsIp") && !results.containsKey("cfsId")){
                results = this.createNFS();
            }
        }catch (Exception e){
            log.error("[服务部署][Nas服务]获取信息失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public Frame runner() throws Exception {
        return this;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        return null;
    }
}
