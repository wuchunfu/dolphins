package com.dolphin.saas.inputs;

import lombok.Data;

@Data
public class KubConfigInputs {
    private String host;
    private Integer https;
    private Integer podMin;
    private Integer podMax;
    private double cpuMax;
    private double cpuMin;
    private Integer memoyMin;
    private Integer memoyMax;
}
