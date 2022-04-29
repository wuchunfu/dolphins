package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_service_deploy_workconfig")
public class ServiceDeployWorkConfig {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "cluster_id")
    private Long clusterId;

    @TableField("cluster_config_name")
    private String clusterConfigName;

    @TableField("cluster_config_address")
    private String clusterConfigAddress;

    @TableField("cluster_config_username")
    private String clusterConfigUsername;

    @TableField("cluster_config_password")
    private String clusterConfigPassword;

    @TableField("cluster_config_token")
    private String clusterConfigToken;

    @TableField("cluster_config_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date clusterConfigCreatetime;

    @TableField("cluster_config_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date clusterConfigUpdatetime;

    @TableField("cluster_config_default")
    private Integer clusterConfigDefault;

    @TableField("cluster_config_types")
    private Integer clusterConfigTypes;
}
