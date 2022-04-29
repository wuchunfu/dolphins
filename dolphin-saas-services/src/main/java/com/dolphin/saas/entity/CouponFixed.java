package com.dolphin.saas.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_coupon_fixed")
public class CouponFixed {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("coupon_id")
    private Long couponId;

    @TableField("uuid")
    private String uuid;

    @TableField("coupon_channelid")
    private Long couponChannelid;

    @TableField("coupon_money")
    private Float couponMoney;

    @TableField("coupon_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date couponCreateTime;

    @TableField("coupon_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date couponUpdatetime;

    @TableField("coupon_status")
    private Integer couponStatus;
}