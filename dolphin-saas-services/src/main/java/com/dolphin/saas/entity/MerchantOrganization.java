package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 用户组织表
 */

@Data
@TableName(value = "ht_merchant_organization")
public class MerchantOrganization {

    @TableId(value = "merchant_id", type = IdType.INPUT)
    private Long merchantId;

    @TableField("uuid")
    private String uuid;

    @TableField("user_type")
    private Integer userType;

    @TableField("user_main")
    private Integer userMain;

    @TableField("status")
    private Integer status;

    @TableField("user_delete")
    private Integer userDelete;

    @TableField("join_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date joinCreatetime;

    @TableField("join_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date joinUpdatetime;
}
