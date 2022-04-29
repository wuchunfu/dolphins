package com.dolphin.saas.commons.clouds.tencent.entity;

import lombok.Data;

@Data
public class WxPayKv {
    private String appId;
    private String mchId;
    private String description;
    private String outTradeNo;
    private String callBackUrl;
    private double money;
}
