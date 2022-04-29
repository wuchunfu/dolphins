package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 资产的ssh服务
 */

@Data
@TableName(value = "ht_cvm_ssh_services")
public class CvmSsh {

    @TableId(value = "cid", type = IdType.INPUT)
    private Integer cid;

    @TableField("service_port")
    private Integer cvmInstanceId;

    @TableField("service_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date service_createtime;

    @TableField("service_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date service_updatetime;

    @TableField("service_username")
    private String service_username;

    @TableField("service_password")
    private String service_password;

    @TableField("service_status")
    private Integer service_status;
}
