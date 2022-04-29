package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_engineer_analyze")
public class EngineerAnalyze {

    @TableId(value = "engineer_id", type = IdType.INPUT)
    private Long engineerId;

    @TableField("engineer_branch")
    private String engineerBranch;

    @TableField("enginner_version")
    private String enginnerVersion;

    @TableField("engineer_type")
    private Integer engineerType;

    @TableField("engineer_rule")
    private String engineerRule;

    @TableField("engineer_severity")
    private Integer engineerSeverity;

    @TableField("engineer_searchfile")
    private String engineerSearchfile;

    @TableField("engineer_code_line")
    private Long engineerCodeLine;

    @TableField("engineer_message")
    private String engineerMessage;

    @TableField("engineer_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date engineerCreatetime;

    @TableField("engineer_tools")
    private String engineerTools;

    @TableField("engineer_code")
    private String engineerCode;
}
