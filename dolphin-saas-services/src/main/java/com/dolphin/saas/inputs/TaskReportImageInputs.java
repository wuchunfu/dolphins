package com.dolphin.saas.inputs;

import lombok.Data;

@Data
public class TaskReportImageInputs {
    private String VulnerabilityID;
    private String PkgName;
    private String InstalledVersion;
    private String FixedVersion;
    private String Severity;
    private String Title;
    private String Description;
    private String References;
}
