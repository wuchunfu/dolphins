package com.dolphin.saas.commons.clouds.tencent;

import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.dnspod.v20210323.DnspodClient;
import com.tencentcloudapi.dnspod.v20210323.models.CreateRecordRequest;
import com.tencentcloudapi.dnspod.v20210323.models.DescribeRecordListRequest;
import com.tencentcloudapi.dnspod.v20210323.models.DescribeRecordListResponse;
import com.tencentcloudapi.dnspod.v20210323.models.RecordListItem;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class DomainServPlugin extends MasterServ implements Frame {
    /**
     * 域名相关的服务
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     */
    public DomainServPlugin(String AccessKeyId, String AccessKeySecret) {
        super(AccessKeyId, AccessKeySecret, 2);
    }

    @Override
    public DomainServPlugin setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    @Override
    public void initService() throws Exception {
    }

    @Override
    public void execService() throws Exception {
        try {
            Integer clusterPayMode = Integer.parseInt(this.paramets.get("clusterPayMode").toString());
            List<String> DomainSubLists = new ArrayList<>();
            switch (clusterPayMode) {
                case 1:
                    DomainSubLists = Arrays.asList("sonar", "nexus", "git", "docker", "jenkins", "yapi");
                    break;

                case 2:
                    DomainSubLists = Arrays.asList("sonar", "nexus", "git", "docker", "jenkins", "yapi", "jumpserver", "skywalking", "sentry");
                    break;

                case 3:
                    DomainSubLists = Arrays.asList("sonar", "nexus", "git", "docker", "jenkins", "yapi", "jumpserver", "skywalking", "sentry", "metersphere", "hfish", "grafana", "zipkin", "es");
                    break;
            }

            for (String SubItems : DomainSubLists) {
                this.createDnsRecord(
                        this.paramets.get("masterDomain").toString(),
                        SubItems + "-" + this.paramets.get("cid").toString(),
                        this.paramets.get("ingressIp").toString()
                );
            }
        } catch (Exception e) {
            log.error("[腾讯云][解析DNS记录]失败: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void finishService() throws Exception {
    }

    @Override
    public void run() throws Exception {

    }

    @Override
    public DomainServPlugin runner() throws Exception {
        Long cid = Long.parseLong(this.paramets.get("cid").toString());
        RedisCommonUtils redisCommonUtils = (RedisCommonUtils) this.paramets.get("redisCommonUtils");

        if (!redisCommonUtils.hasKeys("DomainServPlugin." + cid)) {
            redisCommonUtils.noExpireSset("DomainServPlugin." + cid, 1);
            try {
                this.execService();
            } catch (Exception e) {
                redisCommonUtils.noExpireSset("DomainServPlugin." + cid, 3);
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.noExpireSset("DomainServPlugin." + cid, 2);
            }
        }
        return this;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        return null;
    }

    /**
     * 创建域名记录
     *
     * @param dnsName    主域名地址
     * @param dnsARecord 主机名:wwww
     * @param IpAddr     解析的IP
     * @throws TencentCloudSDKException 抛出的异常
     */
    public void createDnsRecord(String dnsName, String dnsARecord, String IpAddr) throws TencentCloudSDKException {
        try {
            if (!this.checkRecord(dnsName, dnsARecord)){
                // 只有没有才会去创建
                HttpProfile httpProfile = new HttpProfile();
                httpProfile.setEndpoint(this.getTENCENT_DNSPOD_URL());
                ClientProfile clientProfile = new ClientProfile();
                clientProfile.setHttpProfile(httpProfile);
                DnspodClient client = new DnspodClient(this.cred, "", clientProfile);
                CreateRecordRequest createRecordRequest = new CreateRecordRequest();
                createRecordRequest.setDomain(dnsName);
                createRecordRequest.setSubDomain(dnsARecord);
                createRecordRequest.setRecordType("A");
                createRecordRequest.setRecordLine("默认");
                createRecordRequest.setValue(IpAddr);
                client.CreateRecord(createRecordRequest);
            }
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云][DNS解析]域名: {}, A记录: {}, 解析失败: {}", dnsName, dnsARecord, e.getMessage());
        }
    }

    /**
     * 判断记录是否存在
     * @param Domain
     * @param Arecords
     * @return
     * @throws TencentCloudSDKException
     */
    public Boolean checkRecord(String Domain, String Arecords) throws TencentCloudSDKException {
        try{
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_DNSPOD_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            DnspodClient client = new DnspodClient(cred, "", clientProfile);
            DescribeRecordListRequest req = new DescribeRecordListRequest();
            req.setDomain(Domain);
            req.setSubdomain(Arecords);
            req.setRecordType("A");
            // 返回的resp是一个DescribeRecordListResponse的实例，与请求对象对应
            DescribeRecordListResponse resp = client.DescribeRecordList(req);
            return resp.getRecordList().length > 0;
        } catch (TencentCloudSDKException e) {
            return false;
        }
    }
}
