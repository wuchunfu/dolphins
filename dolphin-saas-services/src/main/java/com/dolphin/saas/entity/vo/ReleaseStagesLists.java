package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class ReleaseStagesLists {
    @TableField("release_id")
    private Long releaseId;

    @TableField("stage_id")
    private Long stageId;

    @TableField("stage_name")
    private String stageName;

    @TableField("release_status_icon")
    private String releaseStatusIcon;

    @TableField("stage_status")
    private String stageStatus;
}
