package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class DashboardLists {
    @TableField("name")
    private String name;

    @TableField("time")
    private String time;

    @TableField("status")
    private Integer status;
}
