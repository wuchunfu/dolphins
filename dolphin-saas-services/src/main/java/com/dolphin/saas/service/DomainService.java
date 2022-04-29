package com.dolphin.saas.service;

import com.dolphin.saas.entity.Domain;

import java.util.ArrayList;
import java.util.List;

public interface DomainService {
    // 提交所有的域名 -- 工具API
    void pushDomain(ArrayList<Domain> domainList, String uuid) throws Exception;

    // 获取当前用户存在的域名 -- 工具API
    List<Domain> getOldDomainList(String uuid) throws Exception;

    // 获取所有待抓取的UUID -- 工具API
    ArrayList<String> getDomainUUIDs() throws Exception;
}
