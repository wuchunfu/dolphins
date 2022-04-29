package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class RulesInfo {
    @TableField("rules_info_sort")
    private Long rulesInfoSort;

    @TableField("rules_info_title")
    private String rulesInfoTitle;
}
