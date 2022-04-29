package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_users_active_record")
public class UsersActiveRecord {
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    @TableField("a_info")
    private String Info;

    @TableField("a_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date Createtime;

    @TableField("a_delete")
    private Integer Udelete;
}
