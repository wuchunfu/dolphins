package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class ImageScanLists {
    @TableField("release_id")
    private Long releaseId;

    @TableField("release_version")
    private String releaseVersion;

    @TableField("language_name")
    private String languageName;

    @TableField("engineer_name")
    private String engineerName;

    @TableField("release_job_branch")
    private String releaseJobBranch;
}
