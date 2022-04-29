package com.dolphin.saas.service.impl;

import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.Domain;
import com.dolphin.saas.mapper.DomainMapper;
import com.dolphin.saas.service.DomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service("domainService")
public class DomainServiceimpl extends BaseTools implements DomainService {

    @Resource
    private DomainMapper domainMapper;

    @Override
    public void pushDomain(ArrayList<Domain> domainList, String uuid) throws Exception {
        try {
            for (Domain domain : domainList) {
                domain.setDomainCreatetime(new Date());
                domain.setUuid(uuid);
                if (domainMapper.insert(domain) < 1) {
                    log.error("[DomainService][插入失败]数据:{}", domain);
                    throw new Exception("域名插入失败!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<Domain> getOldDomainList(String uuid) throws Exception {
        return null;
    }

    @Override
    public ArrayList<String> getDomainUUIDs() throws Exception {
        return null;
    }
}
