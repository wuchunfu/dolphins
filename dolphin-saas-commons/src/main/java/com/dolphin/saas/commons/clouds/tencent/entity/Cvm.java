package com.dolphin.saas.commons.clouds.tencent.entity;

import lombok.Data;

@Data
public class Cvm {
    private String reGion;
    private String zone;
    private String VpcId;
    private String SubnetId;
    private String InstanceType;
    private String ImageId;
    private Long BaseDiskSize;
    private Long CloudDiskSize;
    private Long internetAccessible;
    private String PassWord;
    private String SecurityGroupName;
    private Long InStanceCount;
}
