package com.dolphin.saas.entity;
/**
 * 用户表，主要是管理用户信息
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_users")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("uuid")
    private String uuid;

    @TableField("u_login_username")
    private String UserName;

    @TableField("u_login_password")
    private String Password;

    @TableField("u_name")
    private String CommonName;

    @TableField("u_login_status")
    private Integer LoginStatus;

    @TableField("u_email")
    private String Email;

    @TableField("u_phone")
    private Long Phone;

    @TableField("u_createTime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date CreateTime;

    @TableField("u_updateTime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date UpdateTime;

    @TableField("u_lastlogin_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date LastLoginTime;

    @TableField("u_delete")
    private Integer Udelete;

    @TableField("u_header_img")
    private String HeaderImg;

    @TableField("merchant_id")
    private Long MerchantId;

    @TableField("u_identity")
    private Integer Uidentity;

    @TableField("u_money")
    private Float Money;

    @TableField("u_perfect_info")
    private Integer PerfectInfo;

    @TableField("u_user_service_type")
    private Integer UserServiceType;

    @TableField("u_user_service_timeline")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date UserServiceTimeline;
}
