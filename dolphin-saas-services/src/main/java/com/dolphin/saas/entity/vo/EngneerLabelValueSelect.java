package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class EngneerLabelValueSelect {
    @TableField("label")
    private String label;

    @TableField("value")
    private Integer value;

    @TableField("language")
    private String language;
}
