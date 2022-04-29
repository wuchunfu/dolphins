package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class ReleaseLists {
    @TableField("release_id")
    private Long releaseId;

    @TableField("release_version")
    private String releaseVersion;

    @TableField("release_job_namespace")
    private String releaseJobNamespace;

    @TableField("release_job_branch")
    private String releaseJobBranch;

    @TableField("release_job_createtime")
    private Date releaseJobCreatetime;

    @TableField("release_job_status")
    private Integer releaseJobStatus;

    @TableField("release_job_name")
    private String releaseJobName;

    @TableField("release_job_cloud_id")
    private String releaseJobCloudId;
}
