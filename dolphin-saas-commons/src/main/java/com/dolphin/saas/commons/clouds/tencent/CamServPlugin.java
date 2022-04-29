package com.dolphin.saas.commons.clouds.tencent;

import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.service.ClusterService;
import com.tencentcloudapi.cam.v20190116.CamClient;
import com.tencentcloudapi.cam.v20190116.models.*;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.profile.Language;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
public class CamServPlugin extends MasterServ implements Frame {
    // 初始化获取的所有授权清单
    private final ArrayList<String> CamServiceLists = new ArrayList<>();

    public CamServPlugin(String AccessKeyId, String AccessKeySecret) {
        super(AccessKeyId, AccessKeySecret, 2);
    }

    @Override
    public Frame setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    @Override
    public void initService() throws Exception {
        // 获取所有的角色列表
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_CAM_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setLanguage(Language.ZH_CN);
            clientProfile.setHttpProfile(httpProfile);
            CamClient client = new CamClient(this.cred, "", clientProfile);
            DescribeRoleListRequest describeRoleListRequest = new DescribeRoleListRequest();
            describeRoleListRequest.setPage(1L);
            describeRoleListRequest.setRp(100L);
            DescribeRoleListResponse describeRoleListResponse = client.DescribeRoleList(describeRoleListRequest);
            // 输出json格式的字符串回包
            RoleInfo[] respList = describeRoleListResponse.getList();
            if (respList.length > 0) {
                for (RoleInfo roleInfo : respList) {
                    this.CamServiceLists.add(roleInfo.getRoleName());
                }
            }
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云SDK][获取所有的角色列表]异常: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void execService() throws Exception {
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_CAM_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setLanguage(Language.ZH_CN);
            clientProfile.setHttpProfile(httpProfile);
            CamClient client = new CamClient(this.cred, "", clientProfile);

            if (!this.CamServiceLists.contains("TCR_QCSRole")) {
                CreateRoleRequest createRoleRequest = new CreateRoleRequest();
                createRoleRequest.setRoleName("TCR_QCSRole");
                createRoleRequest.set("RoleType", "system");
                createRoleRequest.setPolicyDocument("{\"version\":\"2.0\",\"statement\":[{\"action\":\"name/sts:AssumeRole\",\"effect\":\"allow\",\"principal\":{\"service\":\"tcr.cloud.tencent.com\"}}]}");
                createRoleRequest.setDescription("当前角色为 容器镜像服务 服务角色，该角色将在已关联策略的权限范围内访问您的其他云服务资源。");
                CreateRoleResponse createRoleResponse = client.CreateRole(createRoleRequest);
                if (createRoleResponse.getRoleId() == null) {
                    this.attackTcrRoles(createRoleResponse.getRoleId(), "QcloudAccessForTCRRole");
                }
            }

            if (!this.CamServiceLists.contains("TKE_ALL")) {
                CreateRoleRequest createRoleRequest1 = new CreateRoleRequest();
                createRoleRequest1.setRoleName("TKE_ALL");
                createRoleRequest1.setPolicyDocument("{\"version\":\"2.0\",\"statement\":[{\"action\":\"name/sts:AssumeRole\",\"effect\":\"allow\",\"principal\":{\"service\":[\"cos.qcloud.com\"]}}]}");
                createRoleRequest1.setDescription("TKE全部权限");
                CreateRoleResponse createRoleResponse = client.CreateRole(createRoleRequest1);
                if (createRoleResponse.getRoleId() != null) {
                    this.attackTcrRoles(createRoleResponse.getRoleId(), "QcloudTKEInnerFullAccess");
                    this.attackTcrRoles(createRoleResponse.getRoleId(), "QcloudTKEFullAccess");
                    this.attackTcrRoles(createRoleResponse.getRoleId(), "QcloudAccessForIPAMDRoleInQcloudAllocateEIP");
                    this.attackTcrRoles(createRoleResponse.getRoleId(), "QcloudAccessForTKERole");
                    this.attackTcrRoles(createRoleResponse.getRoleId(), "QcloudProjectKeyFullAccess");
                }
            }

            if (!this.CamServiceLists.contains("TKE_QCSRole")) {
                CreateRoleRequest createRoleRequest2 = new CreateRoleRequest();
                createRoleRequest2.setRoleName("TKE_QCSRole");
                createRoleRequest2.setPolicyDocument("{\"version\":\"2.0\",\"statement\":[{\"action\":\"name/sts:AssumeRole\",\"effect\":\"allow\",\"principal\":{\"service\":\"ccs.qcloud.com\"}}]}");
                createRoleRequest2.setDescription("当前角色为腾讯云容器服务服务角色，该角色将在已关联策略的权限范围内访问您的其他云服务资源。");
                CreateRoleResponse createRoleResponse = client.CreateRole(createRoleRequest2);
                if (createRoleResponse.getRoleId() != null) {
                    this.attackTcrRoles(createRoleResponse.getRoleId(), "QcloudAccessForTKERole");
                    this.attackTcrRoles(createRoleResponse.getRoleId(), "QcloudAccessForTKERoleInOpsManagement");
                }
            }
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云SDK][添加角色]异常: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void finishService() {
        // 最后收尾
    }

    @Override
    public void run() throws Exception {
        ClusterService clusterService = (ClusterService) this.paramets.get("clusterService");
        Long cid = Long.parseLong(this.paramets.get("cid").toString());

        RedisCommonUtils redisCommonUtils = (RedisCommonUtils) this.paramets.get("redisCommonUtils");

        if (!redisCommonUtils.hasKeys("CamServPlugin." + cid)) {
            redisCommonUtils.noExpireSset("CamServPlugin." + cid, 1);
            try {
                clusterService.UpdateStage(1, cid, 1);
                this.initService();
                this.execService();
                this.finishService();
                clusterService.UpdateStage(1, cid, 2);
            } catch (Exception e) {
                clusterService.UpdateStage(1, cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.noExpireSset("CamServPlugin." + cid, 2);
            }
        }
    }

    @Override
    public Frame runner() throws Exception {
        return null;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        // 返回数据
        return null;
    }

    /**
     * 绑定角色授权
     *
     * @param roleId     角色id
     * @param policyName 授权名
     * @return
     */
    protected void attackTcrRoles(String roleId, String policyName) throws TencentCloudSDKException {
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_CAM_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setLanguage(Language.ZH_CN);
            clientProfile.setHttpProfile(httpProfile);
            CamClient client = new CamClient(this.cred, "", clientProfile);
            AttachRolePolicyRequest req = new AttachRolePolicyRequest();
            req.setAttachRoleId(roleId);
            req.setPolicyName(policyName);
            client.AttachRolePolicy(req);
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云SDK][绑定角色授权]异常: {}, 角色: {}, ID: {}", e.getMessage(), policyName, roleId);
            throw new TencentCloudSDKException(e.getMessage());
        }
    }

}
