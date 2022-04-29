package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class CouponReadList {
    @TableField("c_id")
    private Long cId;

    @TableField("c_name")
    private String cName;

    @TableField("c_status")
    private Integer cStatus;
}
