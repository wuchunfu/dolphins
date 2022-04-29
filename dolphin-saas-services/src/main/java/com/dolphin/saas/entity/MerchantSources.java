package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 商户来源表
 */

@Data
@TableName(value = "ht_merchant_sources")
public class MerchantSources {

    @TableId(value = "merchant_source_id", type = IdType.INPUT)
    private Integer merchant_source_id;

    @TableField("merchant_source_name")
    private String merchant_source_name;

    @TableField("merchant_source_phone")
    private Integer merchant_source_phone;

    @TableField("merchant_source_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date merchant_source_createtime;

    @TableField("merchant_source_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date merchant_source_updatetime;

    @TableField("merchant_source_delete_status")
    private Integer merchant_source_delete_status;

    @TableField("merchant_source_remark")
    private String merchant_source_remark;
}
