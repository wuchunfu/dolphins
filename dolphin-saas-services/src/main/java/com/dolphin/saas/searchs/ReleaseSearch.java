package com.dolphin.saas.searchs;

import lombok.Data;

@Data
public class ReleaseSearch {
    private String releaseJobName;
    private String releaseJobCreatetime;
    private String releaseJobUpdatetime;
    private String releaseVersion;
    private int[] releaseJobStatus;
}
