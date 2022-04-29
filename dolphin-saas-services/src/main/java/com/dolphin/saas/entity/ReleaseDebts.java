package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_release_debts")
public class ReleaseDebts {
    @TableId(value = "release_id", type = IdType.INPUT)
    private Long releaseId;

    @TableField("release_debt_id")
    private String releaseDebtId;

    @TableField("release_debt_name")
    private String releaseDebtName;

    @TableField("release_debt_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseDebtCreatetime;

    @TableField("release_debt_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseDebtUpdatetime;

    @TableField("release_debt_status")
    private Integer releaseDebtStatus;

    @TableField("release_debt_star")
    private Float releaseDebtStar;

    @TableField("release_debt_info")
    private String releaseDebtInfo;
}
