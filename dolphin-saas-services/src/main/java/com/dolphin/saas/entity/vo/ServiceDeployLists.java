package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class ServiceDeployLists {

    @TableField("cluster_cloud_id")
    private String clusterCloudId;

    @TableField("cluster_createtime")
    private Date clusterCreatetime;

    @TableField("cluster_current")
    private Integer clusterCurrent;

    @TableField("cluster_id")
    private Long clusterId;

    @TableField("cluster_name")
    private String clusterName;

    @TableField("cluster_instance_id")
    private String clusterInstanceId;

    @TableField("cluster_region_id")
    private String clusterRegionId;

    @TableField("cluster_service_status")
    private Integer clusterServiceStatus;

    @TableField("cluster_type")
    private String clusterType;

    @TableField("cluster_updatetime")
    private Date clusterUpdatetime;

    @TableField("cluster_zone_id")
    private String clusterZoneId;

    @TableField("uuid")
    private String uuid;
}
