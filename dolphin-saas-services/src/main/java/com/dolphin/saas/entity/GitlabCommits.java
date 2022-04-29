package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_gitlab_commits")
public class GitlabCommits {
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    @TableField("uuid")
    private String uuid;

    @TableField("jobs_id")
    private Integer jobsId;

    @TableField("jobs_name")
    private String jobsName;

    @TableField("jobs_commitid")
    private String jobsCommitId;

    @TableField("jobs_shortid")
    private String jobsShortId;

    @TableField("jobs_author")
    private String jobsAuthor;

    @TableField("jobs_create_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date jobsCreateTime;

    @TableField("jobs_change_code_line")
    private Integer jobsChangeCodeLine;
}