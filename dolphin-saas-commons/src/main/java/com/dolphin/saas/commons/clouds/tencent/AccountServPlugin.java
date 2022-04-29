package com.dolphin.saas.commons.clouds.tencent;

import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;
import com.tencentcloudapi.billing.v20180709.BillingClient;
import com.tencentcloudapi.billing.v20180709.models.DescribeAccountBalanceRequest;
import com.tencentcloudapi.billing.v20180709.models.DescribeAccountBalanceResponse;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AccountServPlugin extends MasterServ implements Frame {

    // 余额
    private double priceTotal = 0;

    /**
     * 加载厂商
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     */
    public AccountServPlugin(String AccessKeyId, String AccessKeySecret) {
        super(AccessKeyId, AccessKeySecret, 2);
    }

    @Override
    public Frame setVal(Map<String, Object> paramets) {
        this.paramets = paramets;
        return this;
    }

    @Override
    public void initService() throws Exception {

    }

    @Override
    public void execService() throws Exception {
    }

    @Override
    public void finishService() throws Exception {

    }

    @Override
    public void run() throws Exception {

    }

    @Override
    public Frame runner() throws Exception {
        try {
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(this.getTENCENT_BILL_URL());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            BillingClient client = new BillingClient(this.cred, "", clientProfile);
            DescribeAccountBalanceRequest describeAccountBalanceRequest = new DescribeAccountBalanceRequest();
            DescribeAccountBalanceResponse describeAccountBalanceResponse = client.DescribeAccountBalance(describeAccountBalanceRequest);
            this.priceTotal = (double) describeAccountBalanceResponse.getRealBalance() / 100;
        } catch (TencentCloudSDKException e) {
            log.error("[腾讯云SDK][获取用户账号余额]失败信息: {}", e.getMessage());
            throw new TencentCloudSDKException(e.getMessage());
        }
        return this;
    }

    @Override
    public Map<String, Object> refval() throws Exception {
        Map<String, Object> results = new HashMap<>();
        results.put("money", this.priceTotal);
        return results;
    }
}
