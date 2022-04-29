package com.dolphin.saas.service;

import com.dolphin.saas.entity.MerchantDetails;

public interface MerchantDetailsService {

    // 获取企业完善的信息
    MerchantDetails GetMerchantDetial(String uuid) throws Exception;

    // 修改企业完善的信息
    void ChangeMerchantDetial(MerchantDetails merchantDetails, String uuid) throws Exception;
}
