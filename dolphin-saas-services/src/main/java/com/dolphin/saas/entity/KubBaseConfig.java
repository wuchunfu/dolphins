package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_engineer_kub_base_config")
public class KubBaseConfig {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("engineer_id")
    private Long engineerId;

    @TableField("engineer_ingress_secret_id")
    private String engineerIngressSecretId;

    @TableField("engineer_ingress_secret_name")
    private String engineerIngressSecretName;

    @TableField("engineer_ingress_hostName")
    private String engineerIngressHostName;

    @TableField("engineer_ingress_https")
    private Integer engineerIngressHttps;

    @TableField("engineer_hpa_cpuquota")
    private Integer engineerHpaCpuQuota;

    @TableField("engineer_hpa_maxpod")
    private Integer engineerHpaMaxPod;

    @TableField("engineer_hpa_minpod")
    private Integer engineerHpaMinPod;

    @TableField("engineer_deployment_minpod")
    private Integer engineerDeploymentMinPod;

    @TableField("engineer_deployment_maxpod")
    private Integer engineerDeploymentMaxPod;

    @TableField("engineer_deployment_reqcpu")
    private Double engineerDeploymentReqCpu;

    @TableField("engineer_deployment_limitcpu")
    private Double engineerDeploymentLimitCpu;

    @TableField("engineer_deployment_reqmemory")
    private Integer engineerDeploymentReqMemory;

    @TableField("engineer_deployment_limitmemory")
    private Integer engineerDeploymentLimitMemory;

//    @TableField("engineer_deployment_image_addr")
//    private String engineerDeploymentImageAddr;

    @TableField("engineer_config_namespace")
    private String engineerConfigNameSpace;

    @TableField("engineer_module_name")
    private String engineerModuleName;

    @TableField("engineer_ready")
    private Integer engineerReady;
}
