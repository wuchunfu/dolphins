package com.dolphin.saas.inputs;

import lombok.Data;

@Data
public class CreateCouponInput {
    private Float couponMoney;
    private Integer couponObject;
    private String couponUsers;
    private String couponChannel;
}
