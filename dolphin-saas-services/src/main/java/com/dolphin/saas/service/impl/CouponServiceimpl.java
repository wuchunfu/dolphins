package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.Coupon;
import com.dolphin.saas.entity.CouponFixed;
import com.dolphin.saas.entity.vo.CouponReadList;
import com.dolphin.saas.entity.vo.CouponTips;
import com.dolphin.saas.inputs.CreateCouponInput;
import com.dolphin.saas.mapper.CouponFixedMapper;
import com.dolphin.saas.mapper.CouponMapper;
import com.dolphin.saas.searchs.CouponSearch;
import com.dolphin.saas.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("couponService")
public class CouponServiceimpl extends BaseTools implements CouponService {

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private CouponFixedMapper couponFixedMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCoupon(CreateCouponInput createCouponInput) throws Exception {
        try {
            Coupon coupon = new Coupon();
            coupon.setCouponMoney(createCouponInput.getCouponMoney());
            coupon.setCouponType(createCouponInput.getCouponObject());
            coupon.setCouponStatus(0);
            coupon.setCouponCreateTime(new Date());
            coupon.setCouponHashId(DigestUtils.md5DigestAsHex(new Date().toString().getBytes()));

            if (couponMapper.insert(coupon) < 1) {
                throw new Exception("优惠券新建失败,清检查!");
            }

            if (createCouponInput.getCouponObject() == 0) {
                String[] userLists = createCouponInput.getCouponUsers().split(",");
                for (String userList : userLists) {
                    CouponFixed couponFixed = new CouponFixed();
                    couponFixed.setCouponId(coupon.getCouponId());
                    couponFixed.setCouponMoney(createCouponInput.getCouponMoney());
                    couponFixed.setCouponCreateTime(new Date());
                    couponFixed.setCouponStatus(0);
                    couponFixed.setUuid(userList);
                    if (couponFixedMapper.insert(couponFixed) < 1) {
                        throw new Exception("优惠券新建失败,归属用户清检查!");
                    }
                }
            } else {
                String[] channelLists = createCouponInput.getCouponChannel().split(",");
                for (String channelList : channelLists) {
                    CouponFixed couponFixed = new CouponFixed();
                    couponFixed.setCouponId(coupon.getCouponId());
                    couponFixed.setCouponMoney(createCouponInput.getCouponMoney());
                    couponFixed.setCouponCreateTime(new Date());
                    couponFixed.setCouponStatus(0);
                    couponFixed.setCouponChannelid(Long.parseLong(channelList));
                    if (couponFixedMapper.insert(couponFixed) < 1) {
                        throw new Exception("优惠券新建失败,归属渠道清检查!");
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> FindCouponLists(int Page, int Size, CouponSearch couponSearch) throws Exception {
        // 拼装分页数据
        Map<String, Object> results = new HashMap<>();
        try {
            // 获取分页的数据
            IPage<Coupon> page = new Page<>(Page, Size);
            QueryWrapper<Coupon> queryWrapper = new QueryWrapper<>();

            if (couponSearch != null) {
                if (couponSearch.getCoupon_hashid() != null) {
                    queryWrapper.like("coupon_hashid", couponSearch.getCoupon_hashid());
                }
            }
            // 只展示没有被逻辑删除的数据
            couponMapper.selectPage(page, queryWrapper);

            results.put("page", Page);
            results.put("total", page.getTotal());
            results.put("list", page.getRecords());
            results.put("pageSize", Size);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatusCoupon(Long couponId, Integer status) throws Exception {
        try {
            QueryWrapper<Coupon> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("coupon_id", couponId);
            Coupon coupon = couponMapper.selectOne(queryWrapper);

            if (coupon == null) {
                throw new Exception("无法修改优惠券状态，优惠券不存在!");
            }

            coupon.setCouponUpdateTime(new Date());
            coupon.setCouponStatus(status);
            if (couponMapper.update(coupon, queryWrapper) < 1) {
                throw new Exception("更新优惠券状态失败!");
            }

            // 根据状态判断处理逻辑
            switch (status) {
                case 1:
                    //启用,更新所有人卷
                    QueryWrapper<CouponFixed> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("coupon_id", coupon.getCouponId());
                    queryWrapper1.in("coupon_status", 0, 1, 3);

                    CouponFixed couponFixed = new CouponFixed();
                    couponFixed.setCouponUpdatetime(new Date());
                    couponFixed.setCouponStatus(1);

                    if (couponFixedMapper.update(couponFixed, queryWrapper1) < 1) {
                        throw new Exception("更新优惠券使用人失败!");
                    }
                    break;
                case 2:
                    // 禁用,更新那些没有用的卷
                    QueryWrapper<CouponFixed> queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.eq("coupon_id", coupon.getCouponId());
                    queryWrapper2.in("coupon_status", 0, 1);

                    CouponFixed couponFixed1 = new CouponFixed();
                    couponFixed1.setCouponUpdatetime(new Date());
                    couponFixed1.setCouponStatus(3);

                    if (couponFixedMapper.update(couponFixed1, queryWrapper2) < 1) {
                        throw new Exception("更新优惠券使用人失败!");
                    }
                    break;
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> readCouponItems(Long couponId) throws Exception {
        Map<String, Object> results = new HashMap<>();
        ArrayList<CouponReadList> couponReadLists = new ArrayList<>();
        try {
            QueryWrapper<Coupon> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("coupon_id", couponId);
            Coupon coupon = couponMapper.selectOne(queryWrapper);
            if (coupon == null){
                throw new Exception("获取优惠卷失败!");
            }

            results.put("couponType", coupon.getCouponType());
            results.put("couponMoney", coupon.getCouponMoney());
            results.put("couponCode", coupon.getCouponHashId());
            results.put("couponCreateTime", coupon.getCouponCreateTime());
            results.put("couponUpdateTime", coupon.getCouponUpdateTime());
            results.put("CouponStatus", coupon.getCouponStatus());

            if (coupon.getCouponType() == 1){
                // 获取人对象的数据
                QueryWrapper<CouponFixed> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("coupon_id", couponId);
                couponReadLists = couponFixedMapper.readCouponUserObjectLists(queryWrapper1);
            }
            results.put("couponRecords", couponReadLists);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public ArrayList<CouponTips> FiindAllCouponItems(String uuid) throws Exception {
        ArrayList<CouponTips> couponTipsList;
        try {
            QueryWrapper<CouponFixed> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ht_coupon_fixed.coupon_status", 0);
            queryWrapper.eq("uuid", uuid);
            couponTipsList = couponFixedMapper.selectAllCoupon(queryWrapper);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return couponTipsList;
    }
}
