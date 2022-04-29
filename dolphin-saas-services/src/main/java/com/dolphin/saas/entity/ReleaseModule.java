package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_release_module")
public class ReleaseModule {

    @TableId(value = "release_id", type = IdType.INPUT)
    private Long releaseId;

    @TableField("release_module")
    private String releaseModule;
}
