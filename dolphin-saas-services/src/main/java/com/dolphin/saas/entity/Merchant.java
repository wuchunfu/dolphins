package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 商户表
 */

@Data
@TableName(value = "ht_merchant")
public class Merchant {

    @TableId(value = "merchant_id", type = IdType.AUTO)
    private Long merchantId;

    @TableField("merchant_name")
    private String merchantName;

    @TableField("merchant_type")
    private Integer merchantType;

    @TableField("merchant_attributes")
    private Integer merchantAttributes;

    @TableField("merchant_status")
    private Integer merchantStatus;

    @TableField("merchant_license_picture")
    private String merchantLicensePicture;

    @TableField("merchant_delete_status")
    private Integer merchantDeleteStatus;

    @TableField("merchant_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date merchantCreatetime;

    @TableField("merchant_updtetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date merchantUpdtetime;

    @TableField("merchant_remark")
    private String merchantRemark;

    @TableField("merchant_source_type")
    private Integer merchantSourceType;

    @TableField("merchant_source_id")
    private Integer merchantSourceId;

}
