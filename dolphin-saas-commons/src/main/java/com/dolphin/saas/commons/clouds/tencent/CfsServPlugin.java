package com.dolphin.saas.commons.clouds.tencent;

import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.service.ClusterService;
import com.tencentcloudapi.cfs.v20190719.CfsClient;
import com.tencentcloudapi.cfs.v20190719.models.*;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.profile.Language;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CfsServPlugin extends MasterServ implements Frame {
    // CFS策略组ID
    private String GroupId = "";
    // 返回数据集合
    private final Map<String, Object> OnlineCfs = new HashMap<>();

    /**
     * 加载厂商
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     */
    public CfsServPlugin(String AccessKeyId, String AccessKeySecret) {
        super(AccessKeyId, AccessKeySecret, 2);
    }

    @Override
    public CfsServPlugin setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    @Override
    public void initService() throws Exception {
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_CFS_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setLanguage(Language.ZH_CN);
            clientProfile.setHttpProfile(httpProfile);
            CfsClient client = new CfsClient(this.cred, this.paramets.get("region").toString(), clientProfile);
            DescribeCfsServiceStatusRequest describeCfsServiceStatusRequest = new DescribeCfsServiceStatusRequest();
            DescribeCfsServiceStatusResponse describeCfsServiceStatusResponse = client.DescribeCfsServiceStatus(describeCfsServiceStatusRequest);
            // 如果没有开通就开通下
            if (!describeCfsServiceStatusResponse.getCfsServiceStatus().equals("created")) {
                // 没有开通就创建
                SignUpCfsServiceRequest signUpCfsServiceRequest = new SignUpCfsServiceRequest();
                SignUpCfsServiceResponse signUpCfsServiceResponse = client.SignUpCfsService(signUpCfsServiceRequest);
                if (!signUpCfsServiceResponse.getCfsServiceStatus().equals("created")) {
                    throw new TencentCloudSDKException("开通CFS服务失败!");
                }
            }
            // 查开通过后的id
            DescribeCfsPGroupsRequest describeCfsPGroupsRequest = new DescribeCfsPGroupsRequest();
            DescribeCfsPGroupsResponse describeCfsPGroupsResponse = client.DescribeCfsPGroups(describeCfsPGroupsRequest);

            PGroupInfo[] pGroupList = describeCfsPGroupsResponse.getPGroupList();
            // 把海豚相关的组id加入到列表中
            for (PGroupInfo pGroupInfo : pGroupList) {
                if (pGroupInfo.getName().equals("海豚工程-权限组")) {
                    this.GroupId = pGroupInfo.getPGroupId();
                }
            }

            // 查规则组信息
            if (this.GroupId.equals("")) {
                // 创建规则组和规则
                CreateCfsPGroupRequest createCfsPGroupRequest = new CreateCfsPGroupRequest();
                createCfsPGroupRequest.setName("海豚工程-权限组");
                createCfsPGroupRequest.setDescInfo("海豚工程devops专用");
                CreateCfsPGroupResponse createCfsPGroupResponse = client.CreateCfsPGroup(createCfsPGroupRequest);
                if (createCfsPGroupResponse.getPGroupId() == null) {
                    throw new TencentCloudSDKException("创建CFS规则组失败!");
                }
                CreateCfsRuleRequest createCfsRuleRequest = new CreateCfsRuleRequest();
                createCfsRuleRequest.setPGroupId(createCfsPGroupResponse.getPGroupId());
                createCfsRuleRequest.setRWPermission("RW");
                createCfsRuleRequest.setUserPermission("no_root_squash");
                createCfsRuleRequest.setAuthClientIp("*");
                createCfsRuleRequest.setPriority(1L);
                CreateCfsRuleResponse createCfsRuleResponse = client.CreateCfsRule(createCfsRuleRequest);
                if (createCfsRuleResponse.getRuleId() == null) {
                    throw new TencentCloudSDKException("CFS规则组加入策略失败!");
                }
                this.GroupId = createCfsPGroupResponse.getPGroupId();
            }
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云SDK][CFS初始化]失败: {}", e.getMessage());
            throw new TencentCloudSDKException(e.getMessage());
        }
    }

    @Override
    public void execService() throws Exception {
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_CFS_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setLanguage(Language.ZH_CN);
            clientProfile.setHttpProfile(httpProfile);
            CfsClient client = new CfsClient(this.cred, this.paramets.get("region").toString(), clientProfile);
            // 查文件系统
            DescribeCfsFileSystemsRequest describeCfsFileSystemsRequest = new DescribeCfsFileSystemsRequest();
            DescribeCfsFileSystemsResponse describeCfsFileSystemsResponse = client.DescribeCfsFileSystems(describeCfsFileSystemsRequest);
            if (describeCfsFileSystemsResponse.getTotalCount() != 0) {
                FileSystemInfo[] fileSystemInfos = describeCfsFileSystemsResponse.getFileSystems();
                for (FileSystemInfo fileSystemInfo : fileSystemInfos) {
                    if (fileSystemInfo.getFsName().equals(this.paramets.get("clusterName").toString() + "-cfs")) {
                        this.OnlineCfs.put("cfsId", fileSystemInfo.getFileSystemId());
                    }
                }
            }

            if (!this.OnlineCfs.containsKey("cfsId")) {
                // 创建NFS文件系统
                CreateCfsFileSystemRequest createCfsFileSystemRequest = new CreateCfsFileSystemRequest();
                createCfsFileSystemRequest.setZone(this.paramets.get("zone").toString());
                createCfsFileSystemRequest.setProtocol("NFS");
                createCfsFileSystemRequest.setStorageType("SD");
                createCfsFileSystemRequest.setNetInterface("VPC");
                createCfsFileSystemRequest.setPGroupId(this.GroupId);
                createCfsFileSystemRequest.setVpcId(this.paramets.get("vpcId").toString());
                createCfsFileSystemRequest.setSubnetId(this.paramets.get("subNetId").toString());
                createCfsFileSystemRequest.setFsName(this.paramets.get("clusterName").toString() + "-cfs");
                CreateCfsFileSystemResponse createCfsFileSystemResponse = client.CreateCfsFileSystem(createCfsFileSystemRequest);
                if (createCfsFileSystemResponse.getFileSystemId() == null) {
                    throw new TencentCloudSDKException("创建文件系统CFS失败!");
                } else {
                    this.OnlineCfs.put("cfsId", createCfsFileSystemResponse.getFileSystemId());
                }
            }

            // 查文件系统状态
            DescribeCfsFileSystemsRequest describeCfsFileSystemsRequest1 = new DescribeCfsFileSystemsRequest();
            describeCfsFileSystemsRequest1.setFileSystemId(this.OnlineCfs.get("cfsId").toString());
            // 返回的resp是一个DescribeCfsFileSystemsResponse的实例，与请求对象对应
            DescribeCfsFileSystemsResponse describeCfsFileSystemsResponse1 = client.DescribeCfsFileSystems(describeCfsFileSystemsRequest1);

            if (describeCfsFileSystemsResponse1.getFileSystems()[0].getLifeCycleState().equals("available")){
                try {
                    // 根据文件系统，查挂载点
                    DescribeMountTargetsRequest describeMountTargetsRequest = new DescribeMountTargetsRequest();
                    describeMountTargetsRequest.setFileSystemId(this.OnlineCfs.get("cfsId").toString());
                    DescribeMountTargetsResponse describeMountTargetsResponse = client.DescribeMountTargets(describeMountTargetsRequest);
                    this.OnlineCfs.put("cfsIp", describeMountTargetsResponse
                            .getMountTargets()[0]
                            .getIpAddress());
                } catch (Exception e){
                    log.error("[腾讯云SDK][CFS获取挂载点]失败,稍后再试: {}",e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[腾讯云SDK][CFS创建]失败: {}", e.getMessage());
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

        if (!redisCommonUtils.hasKeys("CfsServPlugin." + cid)) {
            redisCommonUtils.noExpireSset("CfsServPlugin." + cid, 1);
            try {
                clusterService.UpdateStage(4, cid, 1);
                this.initService();
//                this.execService();
                clusterService.UpdateStage(4, cid, 2);
            } catch (Exception e) {
                clusterService.UpdateStage(4, cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.noExpireSset("CfsServPlugin." + cid, 2);
            }
        }
    }

    @Override
    public CfsServPlugin runner() throws Exception {
        this.run();
        return this;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        Map<String, Object> res = new HashMap<>();
        res.put("groupId", this.GroupId);
        return res;
//        return this.OnlineCfs;
    }
}
