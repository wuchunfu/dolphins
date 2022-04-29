package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_withdraw")
public class Withdraw {
    @TableId(value = "withdraw_id", type = IdType.INPUT)
    private Integer withdraw_id;

    @TableField("uuid")
    private String uuid;

    @TableField("withdraw_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date withdraw_createtime;

    @TableField("withdraw_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date withdraw_updatetime;

    @TableField("withdraw_status")
    private Integer withdraw_status;

    @TableField("withdraw_prices")
    private Float withdraw_prices;
}
