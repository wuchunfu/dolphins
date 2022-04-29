package com.dolphin.saas.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "ht_cvm_source")
public class Cvm {
    @TableId(value = "cid", type = IdType.INPUT)
    private Integer cid;

    @TableField("cvm_instance_id")
    private String cvmInstanceId;

    @TableField("cvm_tag_name")
    private String cvmTagName;

    @TableField("cvm_cluster_inside_ip")
    private String cvmClusterInSideIP;

    @TableField("cvm_cluster_outside_ip")
    private String cvmClusterOutSideIP;

    @TableField("cvm_region_id")
    private String cvmRegionId;

    @TableField("cvm_config")
    private String cvmConfig;

    @TableField("cvm_cost")
    private String cvmCost;

    @TableField("cvm_createtime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cvmCreateTime;

    @TableField("cvm_updatetime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cvmUpdateTime;

    @TableField("cvm_region_source")
    private Integer cvmRegionSource;

    @TableField("cvm_status")
    private Integer cvmStatus;

    @TableField("cvm_delete")
    private Integer cvmDelete;

    @TableField("cvm_remark")
    private String cvmRemark;

    @TableField("uuid")
    private String uuid;
}
