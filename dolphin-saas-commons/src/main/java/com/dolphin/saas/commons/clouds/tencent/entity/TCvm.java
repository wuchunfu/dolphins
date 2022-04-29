package com.dolphin.saas.commons.clouds.tencent.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
public class TCvm {
    @JSONField(name = "Version")
    private String Version = "2017-03-12";

    @JSONField(name = "InstanceChargeType")
    private String InstanceChargeType = "POSTPAID_BY_HOUR";

    @JSONField(name = "InstanceChargePrepaid")
    private Map<String, Object> InstanceChargePrepaid = new HashMap<>();

    @JSONField(name = "Placement")
    private Map<String, Object> Placement = new HashMap<>();

    @JSONField(name = "InstanceType")
    private String InstanceType = "S2.4XLARGE32";

    @JSONField(name = "ImageId")
    private String ImageId = "img-hdt9xxkt";

    @JSONField(name = "SystemDisk")
    private Map<String, Object> SystemDisk = new HashMap<>();

    @JSONField(name = "DataDisks")
    private ArrayList DataDisks = new ArrayList();

    @JSONField(name = "VirtualPrivateCloud")
    private Map<String, Object> VirtualPrivateCloud = new HashMap<>();

    @JSONField(name = "InternetAccessible")
    private Map<String, Object> InternetAccessible = new HashMap<>();

    @JSONField(name = "InstanceCount")
    private Integer InstanceCount = 1;

    @JSONField(name = "InstanceName")
    private String InstanceName = "海豚工程-容器Node节点";

    @JSONField(name = "LoginSettings")
    private Map<String, Object> LoginSettings = new HashMap<>();
}
