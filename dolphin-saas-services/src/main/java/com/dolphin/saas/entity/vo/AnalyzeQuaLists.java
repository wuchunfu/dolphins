package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class AnalyzeQuaLists {
    @TableField("engineer_rule")
    private String engineerRule;

    @TableField("counts")
    private Long counts;
}
