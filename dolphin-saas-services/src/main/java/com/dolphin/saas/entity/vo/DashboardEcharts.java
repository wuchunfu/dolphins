package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class DashboardEcharts {
    @TableField("c_days")
    private String Cdays;

    @TableField("counts")
    private Long Counts;
}
