package com.backend.dolphins.common;

import com.dolphin.saas.commons.clouds.tencent.entity.Cvm;
import com.dolphin.saas.commons.clouds.tencent.feature.CvmServ;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.HttpProfile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TencentV1WebApi {
    private final String secreTld;
    private final String secreKey;

    public TencentV1WebApi(String secreTld, String secreKey) {
        this.secreTld = secreTld;
        this.secreKey = secreKey;
    }

    /**
     * 格式化时间参数
     *
     * @param dateTime
     * @return
     */
    private String formatData(Date dateTime) throws Exception {
        try {
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return simpleDateFormat2.format(dateTime);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
