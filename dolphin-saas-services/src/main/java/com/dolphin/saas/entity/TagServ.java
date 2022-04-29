package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_cvm_tag_services")
public class TagServ {
    @TableId(value = "cid", type = IdType.INPUT)
    private Integer cid;

    @TableField("tag_id")
    private Integer tagId;

    @TableField("service_id")
    private String serviceId;

    @TableField("service_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date serviceCreatetime;

    @TableField("service_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date serviceUpdatetime;

    @TableField("service_port")
    private Integer servicePort;

    @TableField("service_info")
    private String serviceInfo;

    @TableField("service_status")
    private Integer serviceStatus;
}