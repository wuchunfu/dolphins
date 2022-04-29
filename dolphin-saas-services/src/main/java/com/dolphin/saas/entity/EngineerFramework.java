package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_engineer_framework")
public class EngineerFramework {
    @TableId(value = "framework_id", type = IdType.INPUT)
    private Integer frameworkId;

    @TableField("framework_name")
    private String frameworkName;

    @TableField("framework_status")
    private Integer frameworkStatus;

    @TableField("framework_language_id")
    private Integer frameworkLanguageId;
}
