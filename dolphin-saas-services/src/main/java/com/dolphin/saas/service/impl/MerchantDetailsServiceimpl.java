package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dolphin.saas.entity.MerchantDetails;
import com.dolphin.saas.mapper.MerchantDetailsMapper;
import com.dolphin.saas.service.MerchantDetailsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service("merchantDetailsService")
public class MerchantDetailsServiceimpl implements MerchantDetailsService {

    @Resource
    private MerchantDetailsMapper merchantDetailsMapper;

    @Override
    public MerchantDetails GetMerchantDetial(String uuid) throws Exception {
        try {
            QueryWrapper<MerchantDetails> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            return merchantDetailsMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            throw new Exception("获取企业信息异常!");
        }
    }

    @Override
    public void ChangeMerchantDetial(MerchantDetails merchantDetails, String uuid) throws Exception {
        try {
            QueryWrapper<MerchantDetails> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);

            MerchantDetails merchantDetails1 = new MerchantDetails();
            if (merchantDetailsMapper.selectCount(queryWrapper) > 0){
                merchantDetails1.setUpdateTime(new Date());
                merchantDetails1.setBankAccount(merchantDetails.getBankAccount());
                merchantDetails1.setBankName(merchantDetails.getBankName());
                merchantDetails1.setIdentificationNumber(merchantDetails.getIdentificationNumber());
                merchantDetails1.setMerchantAddress(merchantDetails.getMerchantAddress());

                if (merchantDetailsMapper.update(merchantDetails1, queryWrapper) < 1) {
                    throw new Exception("更新企业信息失败!");
                }
            }else{
                merchantDetails1.setUuid(uuid);
                merchantDetails1.setCreateTime(new Date());
                merchantDetails1.setBankAccount(merchantDetails.getBankAccount());
                merchantDetails1.setBankName(merchantDetails.getBankName());
                merchantDetails1.setIdentificationNumber(merchantDetails.getIdentificationNumber());
                merchantDetails1.setMerchantAddress(merchantDetails.getMerchantAddress());
                if (merchantDetailsMapper.insert(merchantDetails1) < 1) {
                    throw new Exception("创建企业信息失败!");
                }
            }
        } catch (Exception e) {
            throw new Exception("企业信息处理失败!");
        }
    }
}
