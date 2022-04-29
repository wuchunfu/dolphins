package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dolphin.saas.entity.Merchant;
import com.dolphin.saas.entity.MerchantOrganization;
import com.dolphin.saas.mapper.MerchantMapper;
import com.dolphin.saas.mapper.MerchantOrgMapper;
import com.dolphin.saas.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service("merchantService")
public class MerchantServiceimpl implements MerchantService {

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private MerchantOrgMapper merchantOrgMapper;

    @Override
    public Map<String, Object> JoinIn(Long merchant_id, String uuid) {
        Map<String, Object> results = new HashMap<>();
        // 先查看是否有在组织里，不能重复提交
        QueryWrapper<MerchantOrganization> merchantOrgQueryWrapper = new QueryWrapper<>();
        merchantOrgQueryWrapper.eq("uuid", uuid);
        merchantOrgQueryWrapper.eq("user_delete", 0);
        if (merchantOrgMapper.selectCount(merchantOrgQueryWrapper) > 0) {
            results.put("error", "不能重复加入组织，请先退出旧的组织!");
        } else {
            // 加入组织
            MerchantOrganization merchantOrgItems = new MerchantOrganization();
            merchantOrgItems.setMerchantId(merchant_id);
            merchantOrgItems.setUuid(uuid);
            merchantOrgItems.setJoinCreatetime(new Date());
            if (merchantOrgMapper.insert(merchantOrgItems) < 1) {
                results.put("error", "进入组织失败，请稍后再试!");
            }
        }
        return results;
    }

    // 创建商户
    public Map<String, Object> Create(Merchant merchant, String uuid) {
        Map<String, Object> results = new HashMap<>();
        // 先判断在不在组织里
        QueryWrapper<MerchantOrganization> merchantOrgQueryWrapper = new QueryWrapper<>();
        merchantOrgQueryWrapper.eq("uuid", uuid);
        merchantOrgQueryWrapper.eq("user_delete", 0);
        if (merchantOrgMapper.selectCount(merchantOrgQueryWrapper) > 0) {
            results.put("error", "不能重复加入组织，请先退出旧的组织!");
        } else {
            // 先查商户是否存在
            if (!this.Judgment(merchant.getMerchantName())) {
                // 新增商户
                merchant.setMerchantCreatetime(new Date());
                merchant.setMerchantId(null);
                if (merchantMapper.insert(merchant) < 1) {
                    results.put("error", "商户创建失败!");
                } else {
                    // 查出对应的id
                    QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("merchant_name", merchant.getMerchantName());
                    queryWrapper.select("merchant_id");
                    Merchant merchant1 = merchantMapper.selectOne(queryWrapper);
                    // 将用户+商户id加入到组织
                    MerchantOrganization merchantOrganization = new MerchantOrganization();
                    merchantOrganization.setMerchantId(merchant1.getMerchantId());
                    merchantOrganization.setUuid(uuid);
                    merchantOrganization.setJoinCreatetime(new Date());
                    merchantOrganization.setUserType(1);
                    merchantOrganization.setUserMain(1);
                    if (merchantOrgMapper.insert(merchantOrganization) < 1) {
                        results.put("error", "加入组织失败!");
                    }
                }
            } else {
                results.put("error", "商户已存在!");
            }
        }

        return results;
    }

    @Override
    public Boolean Judgment(String merchant_name) {
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_name", merchant_name);
        return merchantMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public ArrayList LikeMerchantName(String MerchantName) {
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("merchant_name", MerchantName);
        List<Merchant> merchantList = merchantMapper.selectList(queryWrapper);
        ArrayList results = new ArrayList();
        if (merchantList.size() > 0) {
            for (Merchant merchant : merchantList) {
                Map<String, Object> items = new HashMap<>();
                items.put("label", merchant.getMerchantName());
                items.put("value", merchant.getMerchantId());
                items.put("types", merchant.getMerchantType());
                results.add(items);
            }
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void Operate(String uuid, Long merchantId, Integer status, String changeUUid) throws Exception {
        try {
            // 先查这个用户是否是管理员，并在组织内
            QueryWrapper<MerchantOrganization> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("user_type", 1);
            queryWrapper.eq("user_delete", 0);
            queryWrapper.eq("merchant_id", merchantId);

            if (merchantOrgMapper.selectCount(queryWrapper) < 1){
                throw new Exception("您不是组织的管理员无法操作!");
            }

            // 判断被修改用户是否在组织
            QueryWrapper<MerchantOrganization> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("uuid", changeUUid);
            queryWrapper1.eq("user_delete", 0);
            queryWrapper.eq("merchant_id", merchantId);

            if (merchantOrgMapper.selectCount(queryWrapper1) < 1){
                throw new Exception("被修改用户不在组织里!");
            }

            // 修改用户组织状态
            MerchantOrganization merchantOrganization = new MerchantOrganization();
            merchantOrganization.setStatus(status);
            if (merchantOrgMapper.update(merchantOrganization, queryWrapper1) < 1){
                throw new Exception("修改用户状态失败！");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
