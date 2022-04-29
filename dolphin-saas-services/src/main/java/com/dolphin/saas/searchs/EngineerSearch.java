package com.dolphin.saas.searchs;

import lombok.Data;

@Data
public class EngineerSearch {
    /**
     * 工程搜索参数
     */
    private String engineerName;
    private Integer engineerCloudId;
    private Integer engineerLanguageId;
    private Integer engineerFrameworkId;
}
