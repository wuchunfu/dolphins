package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_release_container_analyze")
public class EngineerContainerAnalyze {
    @TableId(value = "release_id", type = IdType.INPUT)
    private Long releaseId;

    @TableField("release_vul_id")
    private String releaseVulId;

    @TableField("release_pkg_name")
    private String releasePkgName;

    @TableField("release_installed_version")
    private String releaseInstalledVersion;

    @TableField("release_fixed_version")
    private String releaseFixedVersion;

    @TableField("release_severity")
    private String releaseSeverity;

    @TableField("release_title")
    private String releaseTitle;

    @TableField("release_description")
    private String releaseDescription;

    @TableField("release_references")
    private String releaseReferences;

    @TableField("release_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseCreateIime;
}
