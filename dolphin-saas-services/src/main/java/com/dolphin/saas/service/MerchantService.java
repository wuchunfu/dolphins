package com.dolphin.saas.service;

import com.dolphin.saas.entity.Merchant;

import java.util.ArrayList;
import java.util.Map;

public interface MerchantService {
    // 创建商户
    Map<String, Object> Create(Merchant merchant, String uuid);

    // 判断是否存在商户
    Boolean Judgment(String merchant_name);

    // 用户加入商户
    Map<String, Object> JoinIn(Long merchant_id, String uuid);

    // 商户模糊查询
    ArrayList LikeMerchantName(String MerchantName);

    // 组织机构操作 -- 前台API
    void Operate(String uuid, Long merchantId, Integer status, String changeUUid) throws Exception;

}
