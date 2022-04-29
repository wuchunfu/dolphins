package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class DockerLists {
    @TableId(value = "dockerfile_id", type = IdType.INPUT)
    private Long dockerfileId;

    @TableField("dockerfile_name")
    private String dockerfileName;

    @TableField("dockerfile_os_path")
    private String dockerfileOsPath;

    @TableField("dockerfile_status")
    private Integer dockerfileStatus;

    @TableField("dockerfile_remark")
    private String dockerfileRemark;

    @TableField("dockerfile_author_id")
    private String dockerfileAuthorId;

    @TableField("dockerfile_money")
    private Float dockerfileMoney;

    @TableField("dockerfile_createtime")
    private Date dockerfileCreatetime;

    @TableField("dockerfile_updatetime")
    private Date dockerfileUpdatetime;

    @TableField("dockerfile_language_name")
    private String dockerfileLanguageName;

    @TableField("dockerfile_framework_name")
    private String dockerfileFrameworkName;
}
