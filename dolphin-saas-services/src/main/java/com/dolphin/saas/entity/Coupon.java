package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_coupon")
public class Coupon {
    @TableId(value = "coupon_id", type = IdType.AUTO)
    private Long couponId;

    @TableField("coupon_type")
    private Integer couponType;

    @TableField("coupon_money")
    private Float couponMoney;

    @TableField("coupon_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date couponCreateTime;

    @TableField("coupon_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date couponUpdateTime;

    @TableField("coupon_hashid")
    private String couponHashId;

    @TableField("coupon_status")
    private Integer couponStatus;
}