package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_release_deploy_stages")
public class ReleaseDeployStages {
    @TableId(value = "release_id", type = IdType.INPUT)
    private Long releaseId;

    @TableField("release_status_id")
    private Long releaseStatusId;

    @TableField("release_status_name")
    private String releaseStatusName;

    @TableField("release_status_icon")
    private String releaseStatusIcon;

    @TableField("release_status_stages")
    private Integer releaseStatusStages;

    @TableField("release_stages_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseStagesCreatetime;

    @TableField("release_stages_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseStagesUpdatetime;
}
