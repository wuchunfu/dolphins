package com.dolphin.saas.inputs;

import lombok.Data;

@Data
public class CreateReleaseInputs {
    private Long engineerId;
    private String branchName;
    private Long clusterId;
    private String nameSpace;
    private String commitId;
    private String authorName;
    private String commitTime;
    private String javaMoule;
}
