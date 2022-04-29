package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class ReleaseItems {
    @TableField("release_id")
    private Long releaseId;

    @TableField("release_job_createtime")
    private Date releaseJobCreatetime;

    @TableField("release_job_updatetime")
    private Date releaseJobUpdatetime;

    @TableField("engineer_name")
    private String engineerName;

    @TableField("release_version")
    private String releaseVersion;

    @TableField("release_job_status")
    private Integer releaseJobStatus;

    @TableField("release_job_branch")
    private String releaseJobBranch;

    @TableField("release_job_namespace")
    private String releaseJobNamespace;

    @TableField("release_commit_author_createtime")
    private Date releaseCommitAuthorCreatetime;

    @TableField("release_commit_author_name")
    private String releaseCommitAuthorName;

    @TableField("release_commit_id")
    private String releaseCommitId;

    @TableField("cluster_name")
    private String clusterName;

    @TableField("cluster_instance_id")
    private String clusterInstanceId;

    @TableField("cluster_region_id")
    private String clusterRegionId;

    @TableField("cluster_zone_id")
    private String clusterZoneId;

    @TableField("release_cloud_name")
    private String releaseCloudName;

    @TableField("release_content")
    private String releaseContent;

    @TableField("release_module")
    private String releaseModule;
}
