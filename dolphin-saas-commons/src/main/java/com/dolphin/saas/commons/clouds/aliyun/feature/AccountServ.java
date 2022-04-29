package com.dolphin.saas.commons.clouds.aliyun.feature;

import com.aliyun.bssopenapi20171214.models.QueryAccountBalanceResponse;
import com.dolphin.saas.commons.clouds.comFinal.Frame;
import com.dolphin.saas.commons.clouds.comFinal.MasterServ;

import java.util.Map;

public class AccountServ extends MasterServ implements Frame {

    private final com.aliyun.bssopenapi20171214.Client client;

    /**
     * 加载厂商
     *
     * @param AccessKeyId     AK的ID
     * @param AccessKeySecret AK的内容
     */
    public AccountServ(String AccessKeyId, String AccessKeySecret) throws Exception{
        super(AccessKeyId, AccessKeySecret, 1);
        // 访问的域名
        this.config.setEndpoint(this.getALIYUN_BUSINESS_URL());
        this.client = new com.aliyun.bssopenapi20171214.Client(this.config);
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

    }

    @Override
    public void finishService() throws Exception {

    }

    @Override
    public void run() throws Exception {
        try {
            QueryAccountBalanceResponse queryAccountBalance = this.client.queryAccountBalance();
            if (!queryAccountBalance.getBody().getCode().equals("200")){
                throw new Exception("账号异常!");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
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
