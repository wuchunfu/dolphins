package com.dolphin.saas.service;

import com.dolphin.saas.entity.Orders;

import java.util.Map;

public interface OrderService {
    // 生成订单
    Orders CreateOrder(String commonId, Integer sourceId, String uuid, Float prices, String details, Integer orderType) throws Exception;

    // 订单列表 -- 后台API
    Map<String, Object> FindOrdersLists(int Pages, int Size) throws Exception;

    // 更新订单的二维码地址 -- 前台APi
    void UpdateOrderCode(String orderId, String codeUrl) throws Exception;

    // 判断是否已经回调 -- 前台API
    void CheckCallBack(Integer orderId) throws Exception;

    // 支付回调服务,更新支付状态 -- 中台API
    void UpdateOrderInfo(String orderId, String transactionId) throws Exception;

    // 用户获取订单列表接口 -- 前台API
    Map<String, Object> FindUserOrdersLists(int Page, int Size, String uuid) throws Exception;

    // 用户获取订单详情接口 -- 前台API
    Map<String, Object> FindUserOrdersInfo(Integer OrderId, String uuid) throws Exception;
}
