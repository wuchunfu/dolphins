package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_service_assets")
public class ServiceDeployAssets {

    @TableId(value = "cluster_id", type = IdType.INPUT)
    private Long clusterId;

    @TableField("cluster_assets_info")
    private String clusterAssetsInfo;
}
