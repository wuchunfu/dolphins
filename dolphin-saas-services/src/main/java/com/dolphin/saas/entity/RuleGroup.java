package com.dolphin.saas.entity;
/**
 * 发布规则策略组
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_release_rules_group")
public class RuleGroup {

    @TableId(value = "rid", type = IdType.AUTO)
    private Long rid;

    @TableField("uuid")
    private String uuid;

    @TableField("rules_name")
    private String RulesName;

    @TableField("rules_type")
    private Integer RulesType;

    @TableField("rules_status")
    private Integer RulesStatus;

    @TableField("rules_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date RulesCreatetime;

    @TableField("rules_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date RulesUpdatetime;

    @TableField("rules_change")
    private Integer RulesChange;

    @TableField("rules_delete")
    private Integer RulesDelete;
}
