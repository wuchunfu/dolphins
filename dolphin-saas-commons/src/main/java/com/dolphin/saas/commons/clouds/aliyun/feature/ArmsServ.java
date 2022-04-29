package com.dolphin.saas.commons.clouds.aliyun.feature;

import com.aliyun.arms20190808.models.OpenArmsServiceRequest;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.service.ClusterService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class ArmsServ extends MasterServ implements Frame {

    private final com.aliyun.arms20190808.Client client;

    public ArmsServ(String AccessKeyId, String AccessKeySecret, Map<String, Object> paramets) throws Exception {
        super(AccessKeyId, AccessKeySecret, 1);
        // 要提前加载
        this.paramets = paramets;

        // 访问的域名
        this.config.setEndpoint(
                this.getALIYUN_ARMS_URL()
                        .replace("{REGIONS}", this.paramets.get("region").toString())
        );
        this.client = new com.aliyun.arms20190808.Client(this.config);
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
            OpenArmsServiceRequest openArmsServiceRequest = new OpenArmsServiceRequest()
                    .setType("arms_app");
            // 复制代码运行请自行打印 API 的返回值
            this.client.openArmsService(openArmsServiceRequest);
            log.info("[阿里云SDK][Arms服务开启]开启成功!");
        } catch (Exception e) {
            List<String> errors = Arrays.asList(
                    "(.*)The request has failed due to a temporary failure of the server(.*)",
                    "(.*)You have already open the service.(.*)"
            );
            boolean check = false;
            for (String items: errors){
                if (e.getMessage().matches(items)){
                    check = true;
                }
            }
            if (!check){
                log.error("[阿里云SDK][Arms服务开启]异常信息: {}", e.getMessage());
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

        if (!redisCommonUtils.hasKeys("ArmsServ." + cid)) {
            redisCommonUtils.noExpireSset("ArmsServ." + cid, 1);
            try {
                clusterService.UpdateStage(3, cid, 1);
                this.execService();
                clusterService.UpdateStage(3, cid, 2);
            } catch (Exception e) {
                clusterService.UpdateStage(3, cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.getRedisTemplate().delete("ArmsServ." + cid);
            }
        }
    }

    @Override
    public Frame runner() throws Exception {
        return null;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        return null;
    }
}
