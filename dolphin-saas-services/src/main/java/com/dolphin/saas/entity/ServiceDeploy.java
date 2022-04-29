package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_service_deploy")
public class ServiceDeploy {
    @TableId(value = "cluster_id", type = IdType.AUTO)
    private Long clusterId;

    @TableField("uuid")
    private String uuid;

    @TableField("cluster_name")
    private String clusterName;

    @TableField("cluster_instance_id")
    private String clusterInstanceId;

    @TableField("cluster_cloud_id")
    private Integer clusterCloudId;

    @TableField("cluster_region_id")
    private String clusterRegionId;

    @TableField("cluster_zone_id")
    private String clusterZoneId;

    @TableField("cluster_current")
    private Integer clusterCurrent;

    @TableField("cluster_type")
    private Integer clusterType;

    @TableField("cluster_delete")
    private Integer clusterDelete;

    @TableField("cluster_pay_mode")
    private Integer clusterPayMode;

    @TableField("cluster_service_status")
    private Integer clusterServiceStatus;

    @TableField("cluster_security_id")
    private String clusterSecurityId;

    @TableField("cluster_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date clusterCreatetime;

    @TableField("cluster_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date clusterUpdatetime;

    @TableField("cluster_deploy_count")
    private Long clusterDeployCount;

}
