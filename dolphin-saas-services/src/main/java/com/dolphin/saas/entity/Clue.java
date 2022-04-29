package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_clue")
public class Clue {
    @TableId(value = "clue_id", type = IdType.AUTO)
    private Long clueId;

    @TableField("clue_username")
    private String clueUserName;

    @TableField("clue_company")
    private String clueCompany;

    @TableField("clue_phonenumber")
    private String cluePhoneNumber;

    @TableField("clue_company_size")
    private Integer clueCompanySize;

    @TableField("clue_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date clueCreateTime;

    @TableField("clue_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date clueUpdateTime;

    @TableField("clue_info")
    private String clueInfo;
}
