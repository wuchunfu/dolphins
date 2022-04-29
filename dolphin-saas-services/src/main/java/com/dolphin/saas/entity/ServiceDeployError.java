package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_service_deploy_errorlog")
public class ServiceDeployError {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("cluster_id")
    private Long clusterId;

    @TableField("cluster_error_log")
    private String clusterErrorLog;

    @TableField("error_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date errorCreatetime;
}
