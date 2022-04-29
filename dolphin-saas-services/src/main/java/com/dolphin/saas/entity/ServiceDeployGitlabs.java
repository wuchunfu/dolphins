package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_service_deploy_gitlabs")
public class ServiceDeployGitlabs {

    @TableId(value = "cluster_id", type = IdType.INPUT)
    private Long clusterId;

    @TableField("git_group_name")
    private String gitGroupName;

    @TableField("git_group_desc")
    private String gitGroupDesc;

    @TableField("git_group_id")
    private Integer gitGroupId;

    @TableField("git_group_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gitGroupCreatetime;
}
