package com.dolphin.saas.inputs;

import lombok.Data;

import java.util.Date;

@Data
public class AlterMessageInputs {
    Date releaseTime;
    String releaseJob;
    String releaseVersion;
    String releaseModule = null;
    String releaseRisk;
    String releaseCommitId;
    String releaseRiskInfo;
    String releaseEnvInfo;
}
