package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_license_manage")
public class License {

    @TableId(value = "license_id", type = IdType.AUTO)
    private Long licenseId;

    @TableField("license_access_key")
    private String licenseAccessKey;

    @TableField("license_access_secret")
    private String licenseAccessSecret;

    @TableField("license_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date licenseCreateTime;

    @TableField("license_status")
    private Integer licenseStatus;

    @TableField("license_version")
    private Integer licenseVersion;

    @TableField("license_deploy_count")
    private Integer licenseDeployCount;

    @TableField("uuid")
    private String uuid;
}
