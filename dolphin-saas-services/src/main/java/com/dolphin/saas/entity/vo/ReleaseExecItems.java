package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ReleaseExecItems {
    @TableId(value = "release_id", type = IdType.INPUT)
    private Long releaseId;

    @TableField("release_version")
    private String releaseVersion;

    @TableField("release_engineer_id")
    private Long releaseEngineerId;

    @TableField("release_job_status")
    private Integer releaseJobStatus;

    @TableField("release_job_branch")
    private String releaseJobBranch;

    @TableField("release_job_cluster_id")
    private Long releaseJobClusterId;

    @TableField("release_job_rollback")
    private String releaseJobRollback;

    @TableField("release_job_namespace")
    private String releaseJobNamespace;

    @TableField("uuid")
    private String uuid;

    @TableField("engineer_name")
    private String engineerName;

    @TableField("engineer_dockerfile_id")
    private Integer engineerDockerfileId;

    @TableField("engineer_giturl")
    private String engineerGiturl;

    @TableField("git_group_name")
    private String gitGroupName;

    @TableField("rules_type")
    private Integer rulesType;
}
