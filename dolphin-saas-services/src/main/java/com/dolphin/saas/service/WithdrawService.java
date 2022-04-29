package com.dolphin.saas.service;

import java.util.Map;

public interface WithdrawService {
    // 创建提现订单
    Map<String, Object> CreateWithRawOrder(String uuid, Float price);
}
