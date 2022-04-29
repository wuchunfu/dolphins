package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 商户审核拒绝表
 */

@Data
@TableName(value = "ht_merchant_audit_failed_logs")
public class MerchantAuditFailedLogs {
    @TableId(value = "merchant_id", type = IdType.INPUT)
    private Long merchant_id;

    @TableField("merchant_audit_info")
    private String merchant_audit_info;

    @TableField("merchant_audit_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date merchant_audit_createtime;

    @TableField("merchant_audit_customer_id")
    private Integer merchant_audit_customer_id;
}
