package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_alter_message_keys")
public class AlterMessage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("key_type")
    private Integer keyType;

    @TableField("key_info")
    private String keyInfo;

    @TableField("key_status")
    private Integer keyStatus;

    @TableField("createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField("updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @TableField("uuid")
    private String uuid;
}
