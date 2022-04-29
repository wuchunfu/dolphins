package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_vendors_types")
public class VendorsType {
    @TableId(value = "tid", type = IdType.INPUT)
    private Integer tid;

    @TableField("t_name")
    private String tName;

    @TableField("t_status")
    private Integer tStatus;
}
