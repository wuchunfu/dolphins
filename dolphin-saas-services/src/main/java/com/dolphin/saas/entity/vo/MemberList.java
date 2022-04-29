package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class MemberList {
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("uuid")
    private String uuid;

    @TableField("u_name")
    private String uName;

    @TableField("u_phone")
    private String uPhone;

    @TableField("u_email")
    private String uEmail;

    @TableField("u_login_status")
    private Integer uLoginStatus;

    @TableField("u_updatetime")
    private Date uUpdateTime;

    @TableField("u_createtime")
    private Date uCreateTime;

    @TableField("u_lastlogin_time")
    private Date uLastloginTime;

    @TableField("u_delete")
    private Integer uDelete;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("u_login_username")
    private String uLoginUserName;
}
