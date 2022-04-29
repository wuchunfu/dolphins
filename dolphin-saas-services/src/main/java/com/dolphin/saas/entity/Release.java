package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_release_jobs")
public class Release {

    @TableId(value = "release_id", type = IdType.AUTO)
    private Long releaseId;

    @TableField("release_version")
    private String releaseVersion;

    @TableField("uuid")
    private String uuid;

    @TableField("release_engineer_id")
    private Long releaseEngineerId;

    @TableField("release_job_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseJobCreatetime;

    @TableField("release_job_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseJobUpdatetime;

    @TableField("release_job_status")
    private Integer releaseJobStatus;

    @TableField("release_job_branch")
    private String releaseJobBranch;

    @TableField("release_job_cluster_id")
    private Long releaseJobClusterId;

    @TableField("release_job_namespace")
    private String releaseJobNamespace;

    @TableField("release_job_rollback")
    private String releaseJobRollback;

    @TableField("release_commit_author_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseCommitTime;

    @TableField("release_commit_author_name")
    private String releaseCommitAuthorName;

    @TableField("release_content")
    private String releaseContent;

    @TableField("release_commit_id")
    private String releaseCommitId;
}
