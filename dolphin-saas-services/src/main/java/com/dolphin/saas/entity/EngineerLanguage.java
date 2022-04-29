package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_engineer_language")
public class EngineerLanguage {
    @TableId(value = "language_id", type = IdType.INPUT)
    private Integer languageId;

    @TableField("language_name")
    private String languageName;

    @TableField("language_status")
    private Integer languageStatus;
}
