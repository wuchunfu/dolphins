package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_engineer_dockerfile")
public class EngineerDockerfile {

    @TableId(value = "dockerfile_id", type = IdType.INPUT)
    private Integer dockerfileId;

    @TableField("dockerfile_name")
    private String dockerfileName;

    @TableField("dockerfile_os_path")
    private String dockerfileOsPath;

    @TableField("dockerfile_status")
    private Integer dockerfileStatus;

    @TableField("dockerfile_remark")
    private String dockerfileRemark;

    @TableField("dockerfile_language_id")
    private Integer dockerfileLanguageId;

    @TableField("dockerfile_framework_id")
    private Integer dockerfileFrameworkId;

    @TableField("dockerfile_author_id")
    private Integer dockerfileAuthorId;

    @TableField("dockerfile_money")
    private Float dockerfileMoney;

    @TableField("dockerfile_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dockerfileCreatetime;

    @TableField("dockerfile_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dockerfileUpdatetime;
}