package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.*;
import com.dolphin.saas.entity.vo.NameValLists;
import com.dolphin.saas.mapper.*;
import com.dolphin.saas.service.LicenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service("licenseService")
public class LicenseServiceimpl extends BaseTools implements LicenseService {

    @Resource
    private LicenseMapper licenseMapper;

    @Resource
    private MemberMapper memberMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private ClueMapper clueMapper;

    @Override
    public Map<String, Object> FindLicenseLists(int Page, int Size, String uuid) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取分页的数据
            IPage<License> page = new Page<>(Page, Size);
            QueryWrapper<License> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            licenseMapper.selectPage(page, queryWrapper);

            response.put("page", Page);
            response.put("total", page.getTotal());
            response.put("list", page.getRecords());
            response.put("pageSize", Size);

        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return response;
    }

    @Override
    public void CreateLicense(int Type, String uuid) throws Exception {
        try {
            License license = new License();
            license.setLicenseCreateTime(new Date());
            license.setLicenseAccessKey(DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes()));
            license.setLicenseAccessSecret(DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes()));
            license.setUuid(uuid);

            if (Type == 1){
                QueryWrapper<License> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("uuid", uuid);
                queryWrapper.eq("license_version", 0);

                if (licenseMapper.selectCount(queryWrapper) > 0){
                    throw new Exception("社区版只能建1个用于体验，不能重复创建，如有特殊需要，请找客服或对接的销售!");
                }
                // 社区版
                license.setLicenseVersion(0);

                if (licenseMapper.insert(license) < 1){
                    throw new Exception("创建License失败，请稍后再试或联系客服!");
                }
            }else if (Type == 2){
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("uuid", uuid);
                queryWrapper.select("merchant_id", "u_phone", "u_name");
                User user = memberMapper.selectOne(queryWrapper);

                QueryWrapper<Merchant> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("merchant_id", user.getMerchantId());

                Merchant merchant = merchantMapper.selectOne(queryWrapper1);

                Clue clue = new Clue();
                clue.setClueCompany(merchant.getMerchantName());
                clue.setClueCreateTime(new Date());
                clue.setCluePhoneNumber(user.getPhone().toString());
                clue.setClueUserName(user.getCommonName());
                clue.setClueCompanySize(1);
                clue.setClueInfo("商业版购买线索!");
                if (clueMapper.insert(clue) < 1) {
                    throw new Exception("记录线索失败,请稍后再试!");
                }
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
