package com.dolphin.saas.service;

import com.dolphin.saas.entity.vo.CouponTips;
import com.dolphin.saas.inputs.CreateCouponInput;
import com.dolphin.saas.searchs.CouponSearch;

import java.util.ArrayList;
import java.util.Map;

public interface CouponService {

    // 创建优惠券 -- 后台API
    void createCoupon(CreateCouponInput createCouponInput) throws Exception;

    // 优惠券列表 -- 后台API
    Map<String, Object> FindCouponLists(int Page, int Size, CouponSearch couponSearch) throws Exception;

    // 优惠券状态切换 -- 后台API
    void changeStatusCoupon(Long couponId, Integer status) throws Exception;

    // 优惠券信息查询 -- 后台API
    Map<String, Object> readCouponItems(Long couponId) throws Exception;

    // 查用户有哪些优惠卷可以使用，用于支付时 -- 前台API
    ArrayList<CouponTips> FiindAllCouponItems(String uuid) throws Exception;
}
