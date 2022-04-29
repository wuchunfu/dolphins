package com.dolphin.saas.commons.clouds.tencent.entity;

import lombok.Data;

import java.util.List;

@Data
public class TkeCluster {
    private String reGion;
    private List cvmJson;
    private String clusterCIDR;
    private String maxNodePodNum;
    private String maxClusterServiceNum;
    private String vpcId;
    private Integer clusterServiceType;
}
