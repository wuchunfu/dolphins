package com.dolphin.saas.commons.clouds.tencent.globs;

import com.dolphin.saas.service.ClusterService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.HttpProfile;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@Data
public class Serv {

    @Resource
    private ClusterService clusterService;

    /**
     * 所有关联的链接
     */

    // 用户
    public final String BILL_URL = "billing.tencentcloudapi.com";
    // CFS
    public final String CFS_URL = "cfs.tencentcloudapi.com";
    // 集群
    private final String TKE_URL = "tke.tencentcloudapi.com";
    // cam
    private final String CAM_URL = "cam.tencentcloudapi.com";

    /**
     * 封装
     */

    private Credential cred;
    private HttpProfile httpProfile;

    public Serv(Credential cred, HttpProfile httpProfile) {
        this.cred = cred;
        this.httpProfile = httpProfile;
    }
}
