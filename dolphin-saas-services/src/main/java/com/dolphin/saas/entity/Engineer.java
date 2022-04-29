package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_engineer")
public class Engineer {
    @TableId(value = "engineer_id", type = IdType.AUTO)
    private Long engineerId;

    @TableField("engineer_name")
    private String engineerName;

    @TableField("uuid")
    private String uuid;

    @TableField("engineer_cloud_id")
    private Integer engineerCloudId;

    @TableField("engineer_language_id")
    private Integer engineerLanguageId;

    @TableField("engineer_framework_id")
    private Integer engineerFrameworkId;

    @TableField("engineer_dockerfile_id")
    private Integer engineerDockerfileId;

    @TableField("engineer_release_rules_id")
    private Integer engineerReleaseRulesId;

    @TableField("engineer_remark")
    private String engineerRemark;

    @TableField("engineer_giturl")
    private String engineerGiturl;

    @TableField("engineer_security")
    private Integer engineerSecurity;

    @TableField("engineer_devops")
    private Integer engineerDevops;

    @TableField("engineer_vocational")
    private Integer engineerVocational;

    @TableField("engineer_testing")
    private Integer engineerTesting;

    @TableField("engineer_codeing")
    private Integer engineerCodeing;

    @TableField("engineer_status")
    private Integer engineerStatus;

    @TableField("engineer_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date engineerCreatetime;

    @TableField("engineer_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date engineerUpdatetime;

    @TableField("engineer_git_id")
    private Long engineerGitId;

    @TableField("engineer_git_group_id")
    private Long engineerGitGroupId;

    @TableField("cluster_id")
    private Long clusterId;

    @TableField("commit_id")
    private String commitId;
}
