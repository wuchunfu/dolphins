package com.dolphin.saas.commons.clouds.tencent.feature;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.dnspod.v20210323.DnspodClient;
import com.tencentcloudapi.dnspod.v20210323.models.CreateRecordRequest;

import java.util.Map;

public class DnsServ {

    private final String DNSPOD_URL = "dnspod.tencentcloudapi.com";
    private final Credential cred;
    private final HttpProfile httpProfile;
    private final ClientProfile clientProfile;

    public DnsServ(Credential cred, HttpProfile httpProfile) {
        this.cred = cred;
        this.httpProfile = httpProfile;
        this.clientProfile = new ClientProfile();
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
            this.httpProfile.setEndpoint(DNSPOD_URL);
            this.clientProfile.setHttpProfile(this.httpProfile);
            DnspodClient client = new DnspodClient(cred, "", clientProfile);
            CreateRecordRequest createRecordRequest = new CreateRecordRequest();
            createRecordRequest.setDomain(dnsName);
            createRecordRequest.setSubDomain(dnsARecord);
            createRecordRequest.setRecordType("A");
            createRecordRequest.setRecordLine("默认");
            createRecordRequest.setValue(IpAddr);
            client.CreateRecord(createRecordRequest);
        } catch (TencentCloudSDKException e) {
            throw new TencentCloudSDKException(e.getMessage());
        }
    }

    public Map<String, Object> getDomainLists() throws TencentCloudSDKException {
        return null;
    }
}
