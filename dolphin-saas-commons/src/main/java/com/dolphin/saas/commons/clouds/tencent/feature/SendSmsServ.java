package com.dolphin.saas.commons.clouds.tencent.feature;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendSmsServ {

    // 定义的常量信息
    private final String SMS_URL = "sms.tencentcloudapi.com";

    private final Credential cred;
    private final HttpProfile httpProfile;

    public SendSmsServ(Credential cred, HttpProfile httpProfile) {
        this.cred = cred;
        this.httpProfile = httpProfile;
    }

    /**
     * 发送短信接口，注册/登陆的接口
     * @param phoneNumber 手机号
     * @param code 验证码
     * @throws Exception
     */
    public void SendCode(String phoneNumber, String code) throws Exception{
        try {
            this.httpProfile.setEndpoint(SMS_URL);
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(this.httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            SmsClient client = new SmsClient(this.cred, "ap-guangzhou", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();
            String[] phoneNumberSet1 = {phoneNumber};
            req.setPhoneNumberSet(phoneNumberSet1);
            req.setSmsSdkAppId("");
            req.setSignName("");
            req.setTemplateId("");
            String[] templateParamSet1 = {code};
            req.setTemplateParamSet(templateParamSet1);
            SendSmsResponse resp = client.SendSms(req);
            if (!resp.getSendStatusSet()[0].getCode().equals("Ok")){
                throw new TencentCloudSDKException("发送失败，请稍后重试!");
            }
        }catch (TencentCloudSDKException e){
            throw new Exception(e.getMessage());
        }
    }

}
