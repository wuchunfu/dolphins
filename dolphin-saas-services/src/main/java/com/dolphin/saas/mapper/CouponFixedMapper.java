package com.dolphin.saas.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dolphin.saas.entity.CouponFixed;
import com.dolphin.saas.entity.vo.CouponReadList;
import com.dolphin.saas.entity.vo.CouponTips;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface CouponFixedMapper extends BaseMapper<CouponFixed> {

    /**
     * 获取所有用户归属的优惠券
     * @param queryWrapper
     * @return
     */
    @Select("SELECT coupon_id, coupon_money, ht_coupon.coupon_hashid coupon_hashid FROM ht_coupon_fixed INNER JOIN ht_coupon ON ht_coupon_fixed.coupon_id = ht_coupon.coupon_id ${ew.customSqlSegment}")
    ArrayList<CouponTips> selectAllCoupon(@Param(Constants.WRAPPER) Wrapper<CouponFixed> queryWrapper);

    /**
     * 后台读取归属优惠券信息
     * @param queryWrapper
     * @return
     */
    @Select("SELECT ht_coupon_fixed.uuid c_id, ht_users.u_name c_name, coupon_status c_status FROM ht_coupon_fixed INNER JOIN ht_users ON ht_users.uuid = ht_coupon_fixed.uuid ${ew.customSqlSegment}")
    ArrayList<CouponReadList> readCouponUserObjectLists(@Param(Constants.WRAPPER) Wrapper<CouponFixed> queryWrapper);
}
