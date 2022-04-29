package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "ht_scan_tasks")
public class ScanTasks {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private String TaskId;

    @TableField("task_scan_target")
    private String TaskScanTarget;

    @TableField("task_scan_type")
    private Integer TaskScanType;

    @TableField("task_scan_keys")
    private String TaskScanKeys;

    @TableField("task_scan_status")
    private Integer TaskScanStatus;
}
