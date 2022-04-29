package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_release_rules_info")
public class RuleInfo {
    @TableId(value = "rules_info_id", type = IdType.AUTO)
    private Long RulesInfoId;

    @TableField("rules_info_title")
    private String RulesInfoTitle;

    @TableField("rules_info_master")
    private Integer RulesInfoMaster;

    @TableField("rules_info_sort")
    private Integer RulesInfoSort;
}
