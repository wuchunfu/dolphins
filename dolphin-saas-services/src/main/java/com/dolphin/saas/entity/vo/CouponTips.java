package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class CouponTips {

    @TableField("coupon_id")
    private Long couponId;

    @TableField("coupon_money")
    private Float couponMoney;

    @TableField("coupon_hashid")
    private String couponHashId;
}
