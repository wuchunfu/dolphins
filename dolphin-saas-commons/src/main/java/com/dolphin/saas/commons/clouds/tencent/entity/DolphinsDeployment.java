package com.dolphin.saas.commons.clouds.tencent.entity;

import lombok.Data;

import java.util.ArrayList;

@Data
public class DolphinsDeployment {
    private String serviceName;
    private String nameSpace;
    private ArrayList<Integer> ports;
    private Integer MaxPod;
    private Integer MinPod;
    private String serviceImageAddr;
    private Double ReqCpu;
    private Integer ReqMemory;
    private Double LimitCpu;
    private Integer LimitMemory;
    private Integer Ready;
}
