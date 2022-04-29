package com.dolphin.saas.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class ReleaseCallbackLists {
    @TableField("release_job_updatetime")
    private Date releaseJobUpdatetime;

    @TableField("release_version")
    private String releaseVersion;
}
