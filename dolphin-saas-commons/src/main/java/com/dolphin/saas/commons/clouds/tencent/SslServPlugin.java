package com.dolphin.saas.commons.clouds.tencent;

import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.dolphin.saas.service.ClusterService;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ssl.v20191205.SslClient;
import com.tencentcloudapi.ssl.v20191205.models.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SslServPlugin extends MasterServ implements Frame {
    private String NexusCertId;
    private String DockerCertId;
    private final Map<String, Object> result = new HashMap<>();

    /**
     * SSl服务
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     */
    public SslServPlugin(String AccessKeyId, String AccessKeySecret) {
        super(AccessKeyId, AccessKeySecret, 2);
    }

    /**
     * 搜索证书是否存在
     * 有就返回ID
     *
     * @param Domain
     * @return
     * @throws TencentCloudSDKException
     */
    public String SearchCert(String Domain) throws TencentCloudSDKException {
        String certId = null;
        try {
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_SSL_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SslClient client = new SslClient(this.cred, "", clientProfile);
            DescribeCertificatesRequest describeCertificatesRequest = new DescribeCertificatesRequest();
            describeCertificatesRequest.setSearchKey(Domain);
            DescribeCertificatesResponse describeCertificatesResponse = client.DescribeCertificates(describeCertificatesRequest);
            Certificates[] certificates = describeCertificatesResponse.getCertificates();

            if (certificates.length > 0) {
                certId = certificates[0].getCertificateId();
            }
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云][SSL服务]异常信息: {}", e.getMessage());
            return null;
        }
        return certId;
    }

    /**
     * 创建SSL证书
     *
     * @param Domain
     * @return
     * @throws TencentCloudSDKException
     */
    public String createSSL(String Domain, String SslDesc) throws TencentCloudSDKException {
        String certId;
        try {
            certId = this.SearchCert(Domain);
            if (certId == null) {
                HttpProfile httpProfile = new HttpProfile();
                httpProfile.setEndpoint(this.getTENCENT_SSL_URL());
                ClientProfile clientProfile = new ClientProfile();
                clientProfile.setHttpProfile(httpProfile);
                SslClient client = new SslClient(this.cred, "", clientProfile);
                // 实例化一个请求对象,每个接口都会对应一个request对象
                ApplyCertificateRequest req = new ApplyCertificateRequest();
                req.setDvAuthMethod("DNS_AUTO");
                req.setDomainName(Domain);
                req.setAlias(SslDesc);
                // 返回的resp是一个ApplyCertificateResponse的实例，与请求对象对应
                ApplyCertificateResponse resp = client.ApplyCertificate(req);
                certId = resp.getCertificateId();
            }
            log.info("ID: {}, 域名: {}", certId, Domain);
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云][SSL服务]异常信息: {}", e.getMessage());
            throw new TencentCloudSDKException(e.getMessage());
        }
        return certId;
    }

    /**
     * 获取证书内容
     *
     * @param certId
     * @return
     * @throws TencentCloudSDKException
     */
    public Map<String, String> getCertFicate(String certId) throws TencentCloudSDKException {
        Map<String, String> results = new HashMap<>();
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_SSL_URL());
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            SslClient client = new SslClient(this.cred, "", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeCertificateDetailRequest req = new DescribeCertificateDetailRequest();
            req.setCertificateId(certId);
            DescribeCertificateDetailResponse resp = client.DescribeCertificateDetail(req);
            results.put("key", resp.getCertificatePrivateKey());
            results.put("crt", resp.getCertificatePublicKey());
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云][SSL服务]异常信息: {}", e.getMessage());
            throw new TencentCloudSDKException(e.getMessage());
        }
        return results;
    }

    /**
     * 获取证书状态
     * 0：审核中，1: 已通过, 2:审核失败
     *
     * @param certId
     * @return
     * @throws TencentCloudSDKException
     */
    public Long getCertStatus(String certId) throws TencentCloudSDKException {
        Long CertStatus;
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_SSL_URL());
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            SslClient client = new SslClient(this.cred, "", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeCertificateRequest describeCertificateRequest = new DescribeCertificateRequest();
            describeCertificateRequest.setCertificateId(certId);
            // 返回的resp是一个DescribeCertificateResponse的实例，与请求对象对应
            DescribeCertificateResponse describeCertificateResponse = client.DescribeCertificate(describeCertificateRequest);
            // 输出json格式的字符串回包
            CertStatus = describeCertificateResponse.getStatus();
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云][SSL服务]异常信息: {}", e.getMessage());
            throw new TencentCloudSDKException(e.getMessage());
        }
        return CertStatus;
    }

    @Override
    public SslServPlugin setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    @Override
    public void initService() throws Exception {

    }

    @Override
    public void execService() throws Exception {
        this.NexusCertId = this.createSSL(this.paramets.get("nexus_domain").toString(), this.paramets.get("cid").toString() + "号集群-Nexus私有仓库");
        if (this.getCertStatus(this.NexusCertId) == 1) {
            this.result.put("nexus", this.getCertFicate(this.NexusCertId));
        }

        this.DockerCertId = this.createSSL(this.paramets.get("docker_domain").toString(), this.paramets.get("cid").toString() + "号集群-docker私有仓库");
        if (this.getCertStatus(this.DockerCertId) == 1) {
            this.result.put("docker", this.getCertFicate(this.DockerCertId));
        }
    }

    @Override
    public void finishService() throws Exception {

    }

    @Override
    public void run() throws Exception {
        Long cid = Long.parseLong(this.paramets.get("cid").toString());
        RedisCommonUtils redisCommonUtils = (RedisCommonUtils) this.paramets.get("redisCommonUtils");

        if (!redisCommonUtils.hasKeys("SslServPlugin." + cid)) {
            redisCommonUtils.noExpireSset("SslServPlugin." + cid, 1);
            try {
                this.execService();
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            } finally {
                // 最后整体收割
                redisCommonUtils.noExpireSset("SslServPlugin." + cid, 2);
            }
        }
    }

    @Override
    public SslServPlugin runner() throws Exception {
        this.run();
        return this;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        return this.result;
    }
}
