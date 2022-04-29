package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_engineer_kub_config")
public class KubConfig {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("engineer_id")
    private Long engineerId;

    @TableField("engineer_config_type")
    private Integer engineerConfigType;

    @TableField("engineer_config_key")
    private String engineerConfigKey;

    @TableField("engineer_config_val")
    private String engineerConfigVal;

    @TableField("engineer_service_name")
    private String engineerServiceName;

    @TableField("engineer_service_port")
    private Integer engineerServicePort;

    @TableField("engineer_service_target_port")
    private Integer engineerServiceTargetPort;

    @TableField("engineer_service_protocol")
    private String engineerServiceProtocol;

    @TableField("engineer_deployment_port")
    private Long engineerDeploymentPort;

    @TableField("engineer_config_namespace")
    private String engineerConfigNameSpace;

    @TableField("engineer_module_name")
    private String engineerModuleName;
}
