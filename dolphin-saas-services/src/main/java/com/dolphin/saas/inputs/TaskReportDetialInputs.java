package com.dolphin.saas.inputs;

import lombok.Data;

@Data
public class TaskReportDetialInputs {
    private String filePath;
    private Long line;
    private String message;
    private String rules;
    private String severity;
    private String type;
    private String code;
    private String branch;
    private String version;
}
