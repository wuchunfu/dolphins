package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "ht_channel")
public class Channel {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("channel_name")
    private String channelName;

    @TableField("channel_type")
    private Integer channelType;

    @TableField("channel_status")
    private Integer channelStatus;

    @TableField("channel_createtime")
    private Date channelCreateTime;

    @TableField("channel_updatetime")
    private Date channelUpdateTime;

    @TableField("channel_hashurl")
    private String channelHashUrl;
}