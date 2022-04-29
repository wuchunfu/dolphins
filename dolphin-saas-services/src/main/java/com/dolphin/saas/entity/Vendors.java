package com.dolphin.saas.entity;
/**
 * 厂商表，主要是存ak相关的信息
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_vendors")
public class Vendors {
    @TableId(value = "vid", type = IdType.INPUT)
    private Integer vid;

    @TableField("type_name")
    private Integer TypeName;

    @TableField("v_access_key")
    private String AccessKey;

    @TableField("v_access_secret")
    private String AccessSecret;

    @TableField("v_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date CreateTime;

    @TableField("v_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date UpdateTime;

    @TableField("v_status")
    private Integer Status;

    @TableField("v_defaults")
    private Integer Vdefaults;

    @TableField("v_delete")
    private Integer Vdelete;

    @TableField("uuid")
    private String uuid;
}
