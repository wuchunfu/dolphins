package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_enginner_kub_config_yaml")
public class KubConfigYaml {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("engineer_id")
    private Long engineerId;

    @TableField("engineer_module_name")
    private String engineerModuleName;

    @TableField("engineer_config_namespace")
    private String engineerConfigNamespace;

    @TableField("engineer_configmap")
    private String engineerConfigmap;

    @TableField("engineer_services")
    private String engineerServices;

    @TableField("engineer_certcrt_info")
    private String engineerCertcrtInfo;

    @TableField("engineer_certkey_info")
    private String engineerCertkeyInfo;

    @TableField("engineer_nginx_info")
    private String engineerNginxInfo;
}
