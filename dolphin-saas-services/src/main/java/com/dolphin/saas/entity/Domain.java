package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_domain")
public class Domain {
    @TableId(value = "id", type = IdType.AUTO)
    private Long Id;

    @TableField("domain_name")
    private String domainName;

    @TableField("domain_source")
    private Integer domainSource;

    @TableField("domain_status")
    private Integer domainStatus;

    @TableField("domain_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date domainCreatetime;

    @TableField("domain_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date domainUpdateTime;

    @TableField("uuid")
    private String Uuid;
}
