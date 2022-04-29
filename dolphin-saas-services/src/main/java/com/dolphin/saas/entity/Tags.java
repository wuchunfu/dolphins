package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_cvm_tags")
public class Tags {
    @TableId(value = "tagid", type = IdType.INPUT)
    private Integer tagId;

    @TableField("tag_name")
    private String tagName;

    @TableField("tag_delete")
    private Integer tagDelete;
}


