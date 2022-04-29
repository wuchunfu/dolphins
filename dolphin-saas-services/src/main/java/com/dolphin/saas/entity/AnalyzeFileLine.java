package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_engineer_analyze_fileline")
public class AnalyzeFileLine {
    @TableId(value = "engineer_id", type = IdType.INPUT)
    private Long engineerId;

    @TableField("engineer_language")
    private String engineerLanguage;

    @TableField("engineer_file")
    private Long engineerFile;

    @TableField("engineer_line")
    private Long engineerLine;

    @TableField("createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField("updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
