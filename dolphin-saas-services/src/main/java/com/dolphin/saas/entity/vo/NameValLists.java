package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class NameValLists {
    @TableField("names")
    private String names;

    @TableField("counts")
    private Long counts;
}
