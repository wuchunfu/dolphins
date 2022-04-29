package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class SelectVal {
    @TableField("label")
    private String label;

    @TableField("value")
    private Integer value;
}
