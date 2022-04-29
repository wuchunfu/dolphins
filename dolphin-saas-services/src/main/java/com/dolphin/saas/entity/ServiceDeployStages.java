package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_service_deploy_stages")
public class ServiceDeployStages {
    @TableId(value = "cluster_id", type = IdType.INPUT)
    private Long clusterId;

    @TableField("stage_pipeline")
    private Integer stagePipeline;

    @TableField("stage_name")
    private String stageName;

    @TableField("stage_info")
    private String stageInfo;

    @TableField("stage_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date stageCreatetime;

    @TableField("stage_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date stageUpdatetime;

    @TableField("stage_status")
    private Integer stageStatus;
}
