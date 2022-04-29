package com.dolphin.saas.service;

import java.util.Map;

public interface LicenseService {
    // 查看license的列表 -- 前台API
    Map<String, Object> FindLicenseLists(int Page, int Size, String uuid) throws Exception;

    // 创建License -- 前台API
    void CreateLicense(int Type, String uuid) throws Exception;
}
