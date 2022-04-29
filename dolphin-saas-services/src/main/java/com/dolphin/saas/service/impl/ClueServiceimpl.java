package com.dolphin.saas.service.impl;

import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.Clue;
import com.dolphin.saas.mapper.ClueMapper;
import com.dolphin.saas.service.ClueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Service("clueService")
public class ClueServiceimpl extends BaseTools implements ClueService {

    @Resource
    private ClueMapper clueMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveReservationInfo(String userName, String phoneNumber, String company, Integer companySize) throws Exception {
        try {
            Clue clue = new Clue();
            clue.setClueCompany(company);
            clue.setClueCreateTime(new Date());
            clue.setCluePhoneNumber(phoneNumber);
            clue.setClueUserName(userName);
            clue.setClueCompanySize(companySize);

            if (clueMapper.insert(clue) < 1) {
                throw new Exception("记录线索失败,请稍后再试!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
