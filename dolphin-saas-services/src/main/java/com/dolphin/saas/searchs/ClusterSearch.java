package com.dolphin.saas.searchs;

import lombok.Data;

@Data
public class ClusterSearch {
    /**
     * 集群搜索参数
     */
    private String clusterInstanceId;
    private Integer clusterCloudId;
    private Integer clusterServiceStatus;
}
