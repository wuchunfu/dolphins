package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_service_deploy_kubernetes_services")
public class ServiceKubsServices {
    @TableId(value = "cluster_service_id", type = IdType.INPUT)
    private String clusterServiceId;

    @TableField("c_service_name")
    private String serviceName;

    @TableField("c_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField("c_namespace_id")
    private Integer serviceId;
}
