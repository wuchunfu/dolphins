package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_release_rules_group_info")
public class RuleGroupInfo {

    @TableId(value = "rid", type = IdType.INPUT)
    private Long rid;

    @TableField("rules_info_id")
    private Long RulesInfoId;

    @TableField("rules_info_change")
    private Integer RulesInfoChange;
}
